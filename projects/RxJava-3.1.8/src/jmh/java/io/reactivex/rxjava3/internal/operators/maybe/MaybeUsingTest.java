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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeUsingTest extends RxJavaTest {

    @Test
    public void resourceSupplierThrows() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw new TestException();
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void errorEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertResult();
    }

    @Test
    public void errorNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertResult();
    }

    @Test
    public void supplierCrashEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierCrashNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierAndDisposerCrashEager() {
        TestObserverEx<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException("Main");
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(list, 0, TestException.class, "Main");
        TestHelper.assertError(list, 1, TestException.class, "Disposer");
    }

    @Test
    public void supplierAndDisposerCrashNonEager() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    throw new TestException("Main");
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException("Disposer");
                }
            }, false).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "Main");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Disposer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        final int[] call = { 0 };
        TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.never();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                call[0]++;
            }
        }, false).test();
        to.dispose();
        assertEquals(1, call[0]);
    }

    @Test
    public void disposeCrashes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return Maybe.never();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException();
                }
            }, false).test();
            to.dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.never();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false));
    }

    @Test
    public void justDisposerCrashes() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyDisposerCrashes() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void errorDisposerCrash() {
        TestObserverEx<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException("Main"));
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(list, 0, TestException.class, "Main");
        TestHelper.assertError(list, 1, TestException.class, "Disposer");
    }

    @Test
    public void doubleOnSubscribe() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return Maybe.wrap(new MaybeSource<Integer>() {

                        @Override
                        public void subscribe(MaybeObserver<? super Integer> observer) {
                            Disposable d1 = Disposable.empty();
                            observer.onSubscribe(d1);
                            Disposable d2 = Disposable.empty();
                            observer.onSubscribe(d2);
                            assertFalse(d1.isDisposed());
                            assertTrue(d2.isDisposed());
                        }
                    });
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, false).test();
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void successDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.lastElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void errorDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.firstElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void emptyDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.firstElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void eagerDisposeResourceThenDisposeUpstream() {
        final StringBuilder sb = new StringBuilder();
        TestObserver<Integer> to = Maybe.using(Functions.justSupplier(1), new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer t) throws Throwable {
                return Maybe.<Integer>never().doOnDispose(new Action() {

                    @Override
                    public void run() throws Throwable {
                        sb.append("Dispose");
                    }
                });
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer t) throws Throwable {
                sb.append("Resource");
            }
        }, true).test();
        to.assertEmpty();
        to.dispose();
        assertEquals("ResourceDispose", sb.toString());
    }

    @Test
    public void nonEagerDisposeUpstreamThenDisposeResource() {
        final StringBuilder sb = new StringBuilder();
        TestObserver<Integer> to = Maybe.using(Functions.justSupplier(1), new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer t) throws Throwable {
                return Maybe.<Integer>never().doOnDispose(new Action() {

                    @Override
                    public void run() throws Throwable {
                        sb.append("Dispose");
                    }
                });
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer t) throws Throwable {
                sb.append("Resource");
            }
        }, false).test();
        to.assertEmpty();
        to.dispose();
        assertEquals("DisposeResource", sb.toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resourceSupplierThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::resourceSupplierThrows, this.description("resourceSupplierThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorEager, this.description("errorEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyEager, this.description("emptyEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNonEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorNonEager, this.description("errorNonEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyNonEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyNonEager, this.description("emptyNonEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierCrashEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::supplierCrashEager, this.description("supplierCrashEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierCrashNonEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::supplierCrashNonEager, this.description("supplierCrashNonEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierAndDisposerCrashEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::supplierAndDisposerCrashEager, this.description("supplierAndDisposerCrashEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierAndDisposerCrashNonEager() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::supplierAndDisposerCrashNonEager, this.description("supplierAndDisposerCrashNonEager"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeCrashes, this.description("disposeCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDisposed, this.description("isDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justDisposerCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justDisposerCrashes, this.description("justDisposerCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDisposerCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyDisposerCrashes, this.description("emptyDisposerCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDisposerCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDisposerCrash, this.description("errorDisposerCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successDisposeRace, this.description("successDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorDisposeRace, this.description("errorDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDisposeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyDisposeRace, this.description("emptyDisposeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerDisposeResourceThenDisposeUpstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::eagerDisposeResourceThenDisposeUpstream, this.description("eagerDisposeResourceThenDisposeUpstream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonEagerDisposeUpstreamThenDisposeResource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nonEagerDisposeUpstreamThenDisposeResource, this.description("nonEagerDisposeUpstreamThenDisposeResource"));
        }

        private MaybeUsingTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeUsingTest();
        }

        @java.lang.Override
        public MaybeUsingTest implementation() {
            return this.implementation;
        }
    }
}
