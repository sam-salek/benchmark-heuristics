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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeFlatMapBiSelectorTest extends RxJavaTest {

    BiFunction<Integer, Integer, String> stringCombine() {
        return new BiFunction<Integer, Integer, String>() {

            @Override
            public String apply(Integer a, Integer b) throws Exception {
                return a + ":" + b;
            }
        };
    }

    @Test
    public void normal() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, stringCombine()).test().assertResult("1:2");
    }

    @Test
    public void normalWithEmpty() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }, stringCombine()).test().assertResult();
    }

    @Test
    public void emptyWithJust() {
        final int[] call = { 0 };
        Maybe.<Integer>empty().flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.just(1);
            }
        }, stringCombine()).test().assertResult();
        assertEquals(0, call[0]);
    }

    @Test
    public void errorWithJust() {
        final int[] call = { 0 };
        Maybe.<Integer>error(new TestException()).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.just(1);
            }
        }, stringCombine()).test().assertFailure(TestException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void justWithError() {
        final int[] call = { 0 };
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.<Integer>error(new TestException());
            }
        }, stringCombine()).test().assertFailure(TestException.class);
        assertEquals(1, call[0]);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().flatMap(new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) throws Exception {
                return b;
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.flatMap(new Function<Object, MaybeSource<Integer>>() {

                    @Override
                    public MaybeSource<Integer> apply(Object v) throws Exception {
                        return Maybe.just(1);
                    }
                }, new BiFunction<Object, Integer, Object>() {

                    @Override
                    public Object apply(Object a, Integer b) throws Exception {
                        return b;
                    }
                });
            }
        });
    }

    @Test
    public void mapperThrows() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }, stringCombine()).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperReturnsNull() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return null;
            }
        }, stringCombine()).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void resultSelectorThrows() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void resultSelectorReturnsNull() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void mapperCancels() {
        final TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                to.dispose();
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                throw new IllegalStateException();
            }
        }).subscribeWith(to).assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalWithEmpty, this.description("normalWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyWithJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyWithJust, this.description("emptyWithJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorWithJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorWithJust, this.description("errorWithJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justWithError, this.description("justWithError"));
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
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrows, this.description("mapperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperReturnsNull, this.description("mapperReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resultSelectorThrows, this.description("resultSelectorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resultSelectorReturnsNull, this.description("resultSelectorReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperCancels, this.description("mapperCancels"));
        }

        private MaybeFlatMapBiSelectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeFlatMapBiSelectorTest();
        }

        @java.lang.Override
        public MaybeFlatMapBiSelectorTest implementation() {
            return this.implementation;
        }
    }
}
