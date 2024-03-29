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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlattenStreamAsFlowableTest extends RxJavaTest {

    @Test
    public void successJust() {
        Single.just(1).flattenStreamAsFlowable(Stream::of).test().assertResult(1);
    }

    @Test
    public void successEmpty() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of()).test().assertResult();
    }

    @Test
    public void successMany() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(2, 3, 4, 5, 6)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void successManyTake() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(2, 3, 4, 5, 6)).take(3).test().assertResult(2, 3, 4);
    }

    @Test
    public void error() throws Throwable {
        @SuppressWarnings("unchecked")
        Function<? super Integer, Stream<? extends Integer>> f = mock(Function.class);
        Single.<Integer>error(new TestException()).flattenStreamAsFlowable(f).test().assertFailure(TestException.class);
        verify(f, never()).apply(any());
    }

    @Test
    public void mapperCrash() {
        Single.just(1).flattenStreamAsFlowable(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.never().flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToFlowable(m -> m.flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(SingleSubject.create().flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void fusedEmpty() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void fusedJust() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1);
    }

    @Test
    public void fusedMany() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3);
    }

    @Test
    public void fusedManyRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.SYNC);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @Test
    public void manyBackpressured() {
        Single.just(1).flattenStreamAsFlowable(v -> IntStream.rangeClosed(1, 5).boxed()).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyBackpressured2() {
        Single.just(1).flattenStreamAsFlowable(v -> IntStream.rangeClosed(1, 5).boxed()).rebatchRequests(1).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedStreamAvailableLater() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertEmpty();
        ss.onSuccess(1);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void fused() throws Throwable {
        AtomicReference<QueueSubscription<Integer>> qsr = new AtomicReference<>();
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsFlowable(Stream::of).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSubscribe(@NonNull Subscription s) {
                qsr.set((QueueSubscription<Integer>) s);
            }
        });
        QueueSubscription<Integer> qs = qsr.get();
        assertEquals(QueueFuseable.ASYNC, qs.requestFusion(QueueFuseable.ASYNC));
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
        ss.onSuccess(1);
        assertFalse(qs.isEmpty());
        assertEquals(1, qs.poll().intValue());
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
        qs.cancel();
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
    }

    @Test
    public void requestOneByOne() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(1, 2, 3, 4, 5)).subscribe(new FlowableSubscriber<Integer>() {

            Subscription upstream;

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                ts.onSubscribe(new BooleanSubscription());
                upstream = s;
                s.request(1);
            }

            @Override
            public void onNext(Integer t) {
                ts.onNext(t);
                upstream.request(1);
            }

            @Override
            public void onError(Throwable t) {
                ts.onError(t);
            }

            @Override
            public void onComplete() {
                ts.onComplete();
            }
        });
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void streamCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Single.just(1).flattenStreamAsFlowable(v -> Stream.of(v).onClose(() -> {
                throw new TestException();
            })).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void hasNextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    throw new TestException();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void nextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                throw new TestException();
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelAfterHasNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    ts.cancel();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).subscribeWith(ts).assertValuesOnly(1);
    }

    @Test
    public void cancelAfterNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                ts.cancel();
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).subscribeWith(ts).assertEmpty();
    }

    @Test
    public void requestSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            SingleSubject<Integer> ss = SingleSubject.create();
            TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            ss.flattenStreamAsFlowable(Stream::of).subscribe(ts);
            Runnable r1 = () -> ss.onSuccess(1);
            Runnable r2 = () -> ts.request(1);
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successJust, this.description("successJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successEmpty, this.description("successEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successMany, this.description("successMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successManyTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successManyTake, this.description("successManyTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperCrash, this.description("mapperCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedEmpty, this.description("fusedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedJust, this.description("fusedJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedMany() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedMany, this.description("fusedMany"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedManyRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedManyRejected, this.description("fusedManyRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressured() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyBackpressured, this.description("manyBackpressured"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressured2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::manyBackpressured2, this.description("manyBackpressured2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedStreamAvailableLater() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedStreamAvailableLater, this.description("fusedStreamAvailableLater"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOneByOne() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestOneByOne, this.description("requestOneByOne"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamCloseCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::streamCloseCrash, this.description("streamCloseCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsInDrain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrowsInDrain, this.description("hasNextThrowsInDrain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsInDrain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextThrowsInDrain, this.description("nextThrowsInDrain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterHasNextInDrain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAfterHasNextInDrain, this.description("cancelAfterHasNextInDrain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterNextInDrain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelAfterNextInDrain, this.description("cancelAfterNextInDrain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestSuccessRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestSuccessRace, this.description("requestSuccessRace"));
        }

        private SingleFlattenStreamAsFlowableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleFlattenStreamAsFlowableTest();
        }

        @java.lang.Override
        public SingleFlattenStreamAsFlowableTest implementation() {
            return this.implementation;
        }
    }
}
