/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package io.reactivex.rxjava3.parallel;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelFromPublisherTest extends RxJavaTest {

    @Test
    public void sourceOverflow() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < 10; i++) {
                    s.onNext(i);
                }
            }
        }.parallel(1, 1).sequential(1).test(0).assertFailure(QueueOverflowException.class);
    }

    @Test
    public void fusedFilterBecomesEmpty() {
        Flowable.just(1).filter(Functions.alwaysFalse()).parallel().sequential().test().assertResult();
    }

    static final class StripBoundary<T> extends Flowable<T> implements FlowableTransformer<T, T> {

        final Flowable<T> source;

        StripBoundary(Flowable<T> source) {
            this.source = source;
        }

        @Override
        public Publisher<T> apply(Flowable<T> upstream) {
            return new StripBoundary<>(upstream);
        }

        @Override
        protected void subscribeActual(Subscriber<? super T> s) {
            source.subscribe(new StripBoundarySubscriber<>(s));
        }

        static final class StripBoundarySubscriber<T> extends BasicFuseableSubscriber<T, T> {

            StripBoundarySubscriber(Subscriber<? super T> downstream) {
                super(downstream);
            }

            @Override
            public void onNext(T t) {
                downstream.onNext(t);
            }

            @Override
            public int requestFusion(int mode) {
                QueueSubscription<T> fs = qs;
                if (fs != null) {
                    int m = fs.requestFusion(mode & ~QueueFuseable.BOUNDARY);
                    this.sourceMode = m;
                    return m;
                }
                return QueueFuseable.NONE;
            }

            @Override
            public T poll() throws Throwable {
                return qs.poll();
            }
        }
    }

    @Test
    public void syncFusedMapCrash() {
        Flowable.just(1).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(new StripBoundary<>(null)).parallel().sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void asyncFusedMapCrash() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.onNext(1);
        up.map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(new StripBoundary<>(null)).parallel().sequential().test().assertFailure(TestException.class);
        assertFalse(up.hasSubscribers());
    }

    @Test
    public void boundaryConfinement() {
        final Set<String> between = new HashSet<>();
        final ConcurrentHashMap<String, String> processing = new ConcurrentHashMap<>();
        TestSubscriberEx<Object> ts = Flowable.range(1, 10).observeOn(Schedulers.single(), false, 1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                between.add(Thread.currentThread().getName());
            }
        }).parallel(2, 1).runOn(Schedulers.computation(), 1).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                processing.putIfAbsent(Thread.currentThread().getName(), "");
                return v;
            }
        }).sequential().to(TestHelper.<Object>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertComplete().assertNoErrors();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(between.toString(), 1, between.size());
        assertTrue(between.toString(), between.iterator().next().contains("RxSingleScheduler"));
        // AnimalSniffer: CHM.keySet() in Java 8 returns KeySetView
        Map<String, String> map = processing;
        for (String e : map.keySet()) {
            assertTrue(map.toString(), e.contains("RxComputationThreadPool"));
        }
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().parallel());
    }

    @Test
    public void syncFusedEmptyPoll() {
        Flowable.just(1, 2).filter(v -> v == 1).compose(TestHelper.flowableStripBoundary()).parallel(1).sequential().test().assertResult(1);
    }

    @Test
    public void asyncFusedEmptyPoll() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.onNext(1);
        up.onNext(2);
        up.onComplete();
        up.filter(v -> v == 1).compose(TestHelper.flowableStripBoundary()).parallel(1).sequential().test().assertResult(1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.parallel().sequential());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestUnboundedRace() {
        FlowableSubscriber<Integer> fs = new FlowableSubscriber<Integer>() {

            @Override
            public void onNext(@NonNull Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                    TestHelper.race(() -> s.request(Long.MAX_VALUE), () -> s.request(Long.MAX_VALUE));
                }
            }
        };
        PublishProcessor.create().parallel(1).subscribe(new FlowableSubscriber[] { fs });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestRace() {
        FlowableSubscriber<Integer> fs = new FlowableSubscriber<Integer>() {

            @Override
            public void onNext(@NonNull Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                    TestHelper.race(() -> s.request(1), () -> s.request(1));
                }
            }
        };
        PublishProcessor.create().parallel(1).subscribe(new FlowableSubscriber[] { fs });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sourceOverflow, this.description("sourceOverflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFilterBecomesEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedFilterBecomesEmpty, this.description("fusedFilterBecomesEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMapCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedMapCrash, this.description("syncFusedMapCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedMapCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedMapCrash, this.description("asyncFusedMapCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryConfinement() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundaryConfinement, this.description("boundaryConfinement"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedEmptyPoll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedEmptyPoll, this.description("syncFusedEmptyPoll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedEmptyPoll() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedEmptyPoll, this.description("asyncFusedEmptyPoll"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestUnboundedRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestUnboundedRace, this.description("requestUnboundedRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestRace, this.description("requestRace"));
        }

        private ParallelFromPublisherTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelFromPublisherTest();
        }

        @java.lang.Override
        public ParallelFromPublisherTest implementation() {
            return this.implementation;
        }
    }
}
