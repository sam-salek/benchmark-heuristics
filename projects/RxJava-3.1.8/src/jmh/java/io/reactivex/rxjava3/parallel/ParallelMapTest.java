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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.ConditionalSubscriber;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ParallelMapTest extends RxJavaTest {

    @Test
    public void subscriberCount() {
        ParallelFlowableTest.checkSubscriberCount(Flowable.range(1, 5).parallel().map(Functions.identity()));
    }

    @Test
    public void doubleFilter() {
        Flowable.range(1, 10).parallel().map(Functions.<Integer>identity()).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().assertResult(6);
    }

    @Test
    public void doubleFilterAsync() {
        Flowable.range(1, 10).parallel().runOn(Schedulers.computation()).map(Functions.<Integer>identity()).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertResult(6);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().map(Functions.<Object>identity()).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleError2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().map(Functions.<Object>identity()).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).parallel().map(Functions.<Object>identity()).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrash() {
        Flowable.just(1).parallel().map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional() {
        Flowable.just(1).parallel().map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional2() {
        Flowable.just(1).parallel().runOn(Schedulers.computation()).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void invalidSubscriberCount() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.range(1, 10).parallel().map(v -> v));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.map(v -> v));
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.map(v -> v).filter(v -> true));
    }

    @Test
    public void conditionalCancelIgnored() {
        Flowable<Integer> f = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Subscriber<@NonNull ? super @NonNull Integer> s) {
                @SuppressWarnings("unchecked")
                ConditionalSubscriber<Integer> subscriber = (ConditionalSubscriber<Integer>) s;
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.tryOnNext(1);
                subscriber.tryOnNext(2);
            }
        };
        ParallelFlowable.fromArray(f).map(v -> {
            throw new TestException();
        }).filter(v -> true).sequential().test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberCount, this.description("subscriberCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilter() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleFilter, this.description("doubleFilter"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilterAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleFilterAsync, this.description("doubleFilterAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError, this.description("doubleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError2, this.description("doubleError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrash, this.description("mapCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrashConditional, this.description("mapCrashConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapCrashConditional2, this.description("mapCrashConditional2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidSubscriberCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::invalidSubscriberCount, this.description("invalidSubscriberCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalCancelIgnored() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::conditionalCancelIgnored, this.description("conditionalCancelIgnored"));
        }

        private ParallelMapTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelMapTest();
        }

        @java.lang.Override
        public ParallelMapTest implementation() {
            return this.implementation;
        }
    }
}
