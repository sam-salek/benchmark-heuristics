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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlatMapIterableObservableTest extends RxJavaTest {

    @Test
    public void normal() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void emptyIterable() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Collections.<Integer>emptyList();
            }
        }).test().assertResult();
    }

    @Test
    public void error() {
        Single.<Integer>error(new TestException()).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).take(1).test().assertResult(1);
    }

    @Test
    public void fused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2);
        ;
    }

    @Test
    public void fusedNoSync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2);
        ;
    }

    @Test
    public void iteratorCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(1, 100, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 1, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 100, 1);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void hasNextCrash2() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 2, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToObservable(new Function<Single<Object>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Single<Object> o) throws Exception {
                return o.flattenAsObservable(new Function<Object, Iterable<Integer>>() {

                    @Override
                    public Iterable<Integer> apply(Object v) throws Exception {
                        return Collections.singleton(1);
                    }
                });
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Collections.singleton(1);
            }
        }));
    }

    @Test
    public void async1() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).hide().observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async2() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async3() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).take(500 * 1000).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async4() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).take(500 * 1000).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void fusedEmptyCheck() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Arrays.asList(1, 2, 3);
            }
        }).subscribe(new Observer<Integer>() {

            QueueDisposable<Integer> qd;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                qd = (QueueDisposable<Integer>) d;
                assertEquals(QueueFuseable.ASYNC, qd.requestFusion(QueueFuseable.ANY));
            }

            @Override
            public void onNext(Integer value) {
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyIterable, this.description("emptyIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::take, this.description("take"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fused, this.description("fused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedNoSync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedNoSync, this.description("fusedNoSync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorCrash, this.description("iteratorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextCrash, this.description("hasNextCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextCrash, this.description("nextCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextCrash2, this.description("hasNextCrash2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async1, this.description("async1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async2, this.description("async2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async3, this.description("async3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::async4, this.description("async4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmptyCheck() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fusedEmptyCheck, this.description("fusedEmptyCheck"));
        }

        private SingleFlatMapIterableObservableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleFlatMapIterableObservableTest();
        }

        @java.lang.Override
        public SingleFlatMapIterableObservableTest implementation() {
            return this.implementation;
        }
    }
}
