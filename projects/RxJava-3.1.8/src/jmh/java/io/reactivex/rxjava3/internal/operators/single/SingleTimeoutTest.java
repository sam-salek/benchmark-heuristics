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

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleTimeoutTest extends RxJavaTest {

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() {
        final PublishSubject<String> subject = PublishSubject.create();
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<String> observer = subject.single("").timeout(100, TimeUnit.MILLISECONDS, scheduler).test();
        assertTrue(subject.hasObservers());
        observer.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void otherErrors() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS, Single.error(new TestException())).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void mainSuccess() {
        Single.just(1).timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void mainError() {
        Single.error(new TestException()).timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void disposeWhenFallback() {
        TestScheduler sch = new TestScheduler();
        SingleSubject<Integer> subj = SingleSubject.create();
        subj.timeout(1, TimeUnit.SECONDS, sch, Single.just(1)).test(true).assertEmpty();
        assertFalse(subj.hasObservers());
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(SingleSubject.create().timeout(1, TimeUnit.DAYS));
    }

    @Test
    public void fallbackDispose() {
        TestScheduler sch = new TestScheduler();
        SingleSubject<Integer> subj = SingleSubject.create();
        SingleSubject<Integer> fallback = SingleSubject.create();
        TestObserver<Integer> to = subj.timeout(1, TimeUnit.SECONDS, sch, fallback).test();
        assertFalse(fallback.hasObservers());
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        assertFalse(subj.hasObservers());
        assertTrue(fallback.hasObservers());
        to.dispose();
        assertFalse(fallback.hasObservers());
    }

    @Test
    public void normalSuccessDoesntDisposeMain() {
        final int[] calls = { 0 };
        Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).timeout(1, TimeUnit.DAYS).test().assertResult(1);
        assertEquals(0, calls[0]);
    }

    @Test
    public void successTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final SingleSubject<Integer> subj = SingleSubject.create();
            SingleSubject<Integer> fallback = SingleSubject.create();
            final TestScheduler sch = new TestScheduler();
            TestObserver<Integer> to = subj.timeout(1, TimeUnit.MILLISECONDS, sch, fallback).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    subj.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (!fallback.hasObservers()) {
                to.assertResult(1);
            } else {
                to.assertEmpty();
            }
        }
    }

    @Test
    public void errorTimeoutRace() {
        final TestException ex = new TestException();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final SingleSubject<Integer> subj = SingleSubject.create();
                SingleSubject<Integer> fallback = SingleSubject.create();
                final TestScheduler sch = new TestScheduler();
                TestObserver<Integer> to = subj.timeout(1, TimeUnit.MILLISECONDS, sch, fallback).test();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        subj.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
                    }
                };
                TestHelper.race(r1, r2);
                if (!fallback.hasObservers()) {
                    to.assertFailure(TestException.class);
                } else {
                    to.assertEmpty();
                }
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mainTimedOut() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS).to(TestHelper.<Object>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertFailureAndMessage(TimeoutException.class, timeoutMessage(1, TimeUnit.MILLISECONDS));
    }

    @Test
    public void mainTimeoutFallbackSuccess() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS, Single.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timeoutBeforeOnSubscribeFromMain() {
        Disposable d = Disposable.empty();
        new Single<Integer>() {

            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super @NonNull Integer> observer) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                observer.onSubscribe(d);
            }
        }.timeout(1, TimeUnit.MILLISECONDS, Single.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        assertTrue(d.isDisposed());
    }

    @Test
    public void timeoutWithZero() throws InterruptedException {
        int n = 10_000;
        Scheduler sch = Schedulers.single();
        for (int i = 0; i < n; i++) {
            final int y = i;
            final CountDownLatch latch = new CountDownLatch(1);
            Disposable d = Single.never().timeout(0, TimeUnit.NANOSECONDS, sch).subscribe(v -> {
            }, e -> {
                // System.out.println("timeout " + y);
                latch.countDown();
            });
            if (!latch.await(2, TimeUnit.SECONDS)) {
                System.out.println(d + " " + sch);
                throw new IllegalStateException("Timeout did not work at y = " + y);
            }
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldUnsubscribeFromUnderlyingSubscriptionOnDispose, this.description("shouldUnsubscribeFromUnderlyingSubscriptionOnDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherErrors, this.description("otherErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainSuccess, this.description("mainSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainError, this.description("mainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhenFallback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeWhenFallback, this.description("disposeWhenFallback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDisposed, this.description("isDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fallbackDispose, this.description("fallbackDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalSuccessDoesntDisposeMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalSuccessDoesntDisposeMain, this.description("normalSuccessDoesntDisposeMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successTimeoutRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successTimeoutRace, this.description("successTimeoutRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorTimeoutRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorTimeoutRace, this.description("errorTimeoutRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainTimedOut() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainTimedOut, this.description("mainTimedOut"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainTimeoutFallbackSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainTimeoutFallbackSuccess, this.description("mainTimeoutFallbackSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutBeforeOnSubscribeFromMain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutBeforeOnSubscribeFromMain, this.description("timeoutBeforeOnSubscribeFromMain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutWithZero() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeoutWithZero, this.description("timeoutWithZero"));
        }

        private SingleTimeoutTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleTimeoutTest();
        }

        @java.lang.Override
        public SingleTimeoutTest implementation() {
            return this.implementation;
        }
    }
}
