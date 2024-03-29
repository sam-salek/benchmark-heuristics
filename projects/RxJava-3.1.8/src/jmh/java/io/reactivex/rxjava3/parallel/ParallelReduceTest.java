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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ParallelReduceTest extends RxJavaTest {

    @Test
    public void subscriberCount() {
        ParallelFlowableTest.checkSubscriberCount(Flowable.range(1, 5).parallel().reduce(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                return new ArrayList<>();
            }
        }, new BiFunction<List<Integer>, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(List<Integer> a, Integer b) throws Exception {
                a.add(b);
                return a;
            }
        }));
    }

    @Test
    public void initialCrash() {
        Flowable.range(1, 5).parallel().reduce(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                throw new TestException();
            }
        }, new BiFunction<List<Integer>, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(List<Integer> a, Integer b) throws Exception {
                a.add(b);
                return a;
            }
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void reducerCrash() {
        Flowable.range(1, 5).parallel().reduce(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                return new ArrayList<>();
            }
        }, new BiFunction<List<Integer>, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(List<Integer> a, Integer b) throws Exception {
                if (b == 3) {
                    throw new TestException();
                }
                a.add(b);
                return a;
            }
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = pp.parallel().reduce(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                return new ArrayList<>();
            }
        }, new BiFunction<List<Integer>, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(List<Integer> a, Integer b) throws Exception {
                a.add(b);
                return a;
            }
        }).sequential().test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void error() {
        Flowable.<Integer>error(new TestException()).parallel().reduce(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                return new ArrayList<>();
            }
        }, new BiFunction<List<Integer>, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(List<Integer> a, Integer b) throws Exception {
                a.add(b);
                return a;
            }
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().reduce(new Supplier<List<Object>>() {

                @Override
                public List<Object> get() throws Exception {
                    return new ArrayList<>();
                }
            }, new BiFunction<List<Object>, Object, List<Object>>() {

                @Override
                public List<Object> apply(List<Object> a, Object b) throws Exception {
                    a.add(b);
                    return a;
                }
            }).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(pf -> pf.reduce(ArrayList::new, (a, b) -> a));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subscriberCount, this.description("subscriberCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initialCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::initialCrash, this.description("initialCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reducerCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::reducerCrash, this.description("reducerCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleError, this.description("doubleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private ParallelReduceTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ParallelReduceTest();
        }

        @java.lang.Override
        public ParallelReduceTest implementation() {
            return this.implementation;
        }
    }
}
