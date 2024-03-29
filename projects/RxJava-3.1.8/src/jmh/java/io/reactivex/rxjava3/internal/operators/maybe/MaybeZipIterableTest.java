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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.util.CrashingMappedIterable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeZipIterableTest extends RxJavaTest {

    final Function<Object[], Object> addString = new Function<Object[], Object>() {

        @Override
        public Object apply(Object[] a) throws Exception {
            return Arrays.toString(a);
        }
    };

    @Test
    public void firstError() {
        Maybe.zip(Arrays.asList(Maybe.error(new TestException()), Maybe.just(1)), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void secondError() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.<Integer>error(new TestException())), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Object> to = Maybe.zip(Arrays.asList(pp.singleElement(), pp.singleElement()), addString).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void zipperThrows() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.just(2)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void zipperReturnsNull() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.just(2)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void middleError() {
        PublishProcessor<Integer> pp0 = PublishProcessor.create();
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        TestObserver<Object> to = Maybe.zip(Arrays.asList(pp0.singleElement(), pp1.singleElement(), pp0.singleElement()), addString).test();
        pp1.onError(new TestException());
        assertFalse(pp0.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void innerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp0 = PublishProcessor.create();
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final TestObserver<Object> to = Maybe.zip(Arrays.asList(pp0.singleElement(), pp1.singleElement()), addString).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp0.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void iteratorThrows() {
        Maybe.zip(new CrashingMappedIterable<>(1, 100, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextThrows() {
        Maybe.zip(new CrashingMappedIterable<>(100, 20, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextThrows() {
        Maybe.zip(new CrashingMappedIterable<>(100, 100, 5, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableOneIsNull() {
        Maybe.zip(Arrays.asList(null, Maybe.just(1)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableTwoIsNull() {
        Maybe.zip(Arrays.asList(Maybe.just(1), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test
    public void singleSourceZipperReturnsNull() {
        Maybe.zipArray(Functions.justFunction(null), Maybe.just(1)).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }

    @Test
    public void maybeSourcesInIterable() {
        MaybeSource<Integer> source = new MaybeSource<Integer>() {

            @Override
            public void subscribe(MaybeObserver<? super Integer> observer) {
                Maybe.just(1).subscribe(observer);
            }
        };
        Maybe.zip(Arrays.asList(source, source), new Function<Object[], Integer>() {

            @Override
            public Integer apply(Object[] t) throws Throwable {
                return 2;
            }
        }).test().assertResult(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstError, this.description("firstError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::secondError, this.description("secondError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zipperThrows, this.description("zipperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zipperReturnsNull, this.description("zipperReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_middleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::middleError, this.description("middleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerErrorRace, this.description("innerErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::iteratorThrows, this.description("iteratorThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasNextThrows, this.description("hasNextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::nextThrows, this.description("nextThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableOneIsNull, this.description("zipIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableTwoIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableTwoIsNull, this.description("zipIterableTwoIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceZipperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSourceZipperReturnsNull, this.description("singleSourceZipperReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSourcesInIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maybeSourcesInIterable, this.description("maybeSourcesInIterable"));
        }

        private MaybeZipIterableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeZipIterableTest();
        }

        @java.lang.Override
        public MaybeZipIterableTest implementation() {
            return this.implementation;
        }
    }
}
