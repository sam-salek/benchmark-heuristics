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
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeConcatArrayTest extends RxJavaTest {

    @Test
    public void cancel() {
        Maybe.concatArray(Maybe.just(1), Maybe.just(2)).take(1).test().assertResult(1);
    }

    @Test
    public void cancelDelayError() {
        Maybe.concatArrayDelayError(Maybe.just(1), Maybe.just(2)).take(1).test().assertResult(1);
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Maybe.concatArray(Maybe.just(1), Maybe.just(2)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(2);
        ts.assertResult(1, 2);
    }

    @Test
    public void backpressureDelayError() {
        TestSubscriber<Integer> ts = Maybe.concatArrayDelayError(Maybe.just(1), Maybe.just(2)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(2);
        ts.assertResult(1, 2);
    }

    @Test
    public void requestCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = Maybe.concatArray(Maybe.just(1), Maybe.just(2)).test(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void requestCancelRaceDelayError() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = Maybe.concatArrayDelayError(Maybe.just(1), Maybe.just(2)).test(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void errorAfterTermination() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final MaybeObserver<?>[] o = { null };
            Maybe.concatArrayDelayError(Maybe.just(1), Maybe.error(new IOException()), new Maybe<Integer>() {

                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onSuccess(2);
                    o[0] = observer;
                }
            }).test().assertFailure(IOException.class, 1, 2);
            o[0].onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };
        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {

            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Maybe.concatArray(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionDelayError() {
        final int[] calls = { 0 };
        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {

            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Maybe.concatArrayDelayError(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Maybe.concatArray(MaybeSubject.create(), MaybeSubject.create()));
    }

    @Test
    public void badRequestDelayError() {
        TestHelper.assertBadRequestReported(Maybe.concatArrayDelayError(MaybeSubject.create(), MaybeSubject.create()));
    }

    @Test
    public void mixed() {
        Maybe.concatArray(Maybe.just(1), Maybe.empty(), Maybe.just(2), Maybe.empty(), Maybe.empty()).test().assertResult(1, 2);
    }

    @Test
    public void requestBeforeSuccess() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = Maybe.concatArray(ms, ms).test();
        ts.assertEmpty();
        ms.onSuccess(1);
        ts.assertResult(1, 1);
    }

    @Test
    public void requestBeforeComplete() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = Maybe.concatArray(ms, ms).test();
        ts.assertEmpty();
        ms.onComplete();
        ts.assertResult();
    }

    @Test
    public void requestBeforeSuccessDelayError() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = Maybe.concatArrayDelayError(ms, ms).test();
        ts.assertEmpty();
        ms.onSuccess(1);
        ts.assertResult(1, 1);
    }

    @Test
    public void requestBeforeCompleteDelayError() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = Maybe.concatArrayDelayError(ms, ms).test();
        ts.assertEmpty();
        ms.onComplete();
        ts.assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelDelayError, this.description("cancelDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureDelayError, this.description("backpressureDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCancelRace, this.description("requestCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRaceDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestCancelRaceDelayError, this.description("requestCancelRaceDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAfterTermination() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorAfterTermination, this.description("errorAfterTermination"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noSubsequentSubscription, this.description("noSubsequentSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noSubsequentSubscriptionDelayError, this.description("noSubsequentSubscriptionDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequestDelayError, this.description("badRequestDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mixed, this.description("mixed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBeforeSuccess, this.description("requestBeforeSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBeforeComplete, this.description("requestBeforeComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeSuccessDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBeforeSuccessDelayError, this.description("requestBeforeSuccessDelayError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeCompleteDelayError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestBeforeCompleteDelayError, this.description("requestBeforeCompleteDelayError"));
        }

        private MaybeConcatArrayTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeConcatArrayTest();
        }

        @java.lang.Override
        public MaybeConcatArrayTest implementation() {
            return this.implementation;
        }
    }
}
