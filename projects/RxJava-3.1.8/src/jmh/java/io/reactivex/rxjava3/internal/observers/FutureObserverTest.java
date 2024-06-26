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
package io.reactivex.rxjava3.internal.observers;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FutureObserverTest extends RxJavaTest {

    FutureObserver<Integer> fo;

    @Before
    public void before() {
        fo = new FutureObserver<>();
    }

    @Test
    public void cancel2() {
        fo.dispose();
        assertFalse(fo.isCancelled());
        assertFalse(fo.isDisposed());
        assertFalse(fo.isDone());
        for (int i = 0; i < 2; i++) {
            fo.cancel(i == 0);
            assertTrue(fo.isCancelled());
            assertTrue(fo.isDisposed());
            assertTrue(fo.isDone());
        }
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onNext(1);
            fo.onError(new TestException("First"));
            fo.onError(new TestException("Second"));
            fo.onComplete();
            assertTrue(fo.isCancelled());
            assertTrue(fo.isDisposed());
            assertTrue(fo.isDone());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancel() throws Exception {
        assertFalse(fo.isDone());
        assertFalse(fo.isCancelled());
        fo.cancel(false);
        assertTrue(fo.isDone());
        assertTrue(fo.isCancelled());
        try {
            fo.get();
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
        try {
            fo.get(1, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
            // expected
        }
    }

    @Test
    public void onError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onError(new TestException("One"));
            fo.onError(new TestException("Two"));
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
                assertEquals("One", ex.getCause().getMessage());
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNext() throws Exception {
        fo.onNext(1);
        fo.onComplete();
        assertEquals(1, fo.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void onSubscribe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposable.empty();
            fo.onSubscribe(d1);
            Disposable d2 = Disposable.empty();
            fo.onSubscribe(d2);
            assertFalse(d1.isDisposed());
            assertTrue(d2.isDisposed());
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureObserver<Integer> fo = new FutureObserver<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fo.cancel(false);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void await() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fo.onNext(1);
                fo.onComplete();
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, fo.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void onErrorCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final FutureObserver<Integer> fo = new FutureObserver<>();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        fo.cancel(false);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        fo.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final FutureObserver<Integer> fo = new FutureObserver<>();
                if (i % 3 == 0) {
                    fo.onSubscribe(Disposable.empty());
                }
                if (i % 2 == 0) {
                    fo.onNext(1);
                }
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        fo.cancel(false);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        fo.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorOnComplete() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onError(new TestException("One"));
            fo.onComplete();
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
                assertEquals("One", ex.getCause().getMessage());
            }
            TestHelper.assertUndeliverable(errors, 0, NoSuchElementException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onComplete();
            fo.onError(new TestException("One"));
            try {
                assertNull(fo.get(5, TimeUnit.MILLISECONDS));
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof NoSuchElementException);
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onNext(1);
            fo.onComplete();
            fo.onError(new TestException("One"));
            assertEquals((Integer) 1, fo.get(5, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.cancel(true);
            fo.onError(new TestException("One"));
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
                fail("Should have thrown");
            } catch (CancellationException ex) {
                // expected
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelOnComplete() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.cancel(true);
            fo.onComplete();
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
                fail("Should have thrown");
            } catch (CancellationException ex) {
                // expected
            }
            TestHelper.assertUndeliverable(errors, 0, NoSuchElementException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextThenOnCompleteTwice() throws Exception {
        fo.onNext(1);
        fo.onComplete();
        fo.onComplete();
        assertEquals(1, fo.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test(expected = InterruptedException.class)
    public void getInterrupted() throws Exception {
        Thread.currentThread().interrupt();
        fo.get();
    }

    @Test
    public void completeAsync() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fo.onNext(1);
                fo.onComplete();
            }
        }, 500, TimeUnit.MILLISECONDS);
        assertEquals(1, fo.get().intValue());
    }

    @Test
    public void getTimedOut() throws Exception {
        try {
            fo.get(1, TimeUnit.NANOSECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(1, TimeUnit.NANOSECONDS), expected.getMessage());
        }
    }

    @Test
    public void cancelOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureObserver<Integer> fo = new FutureObserver<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fo.cancel(false);
                }
            };
            Disposable d = Disposable.empty();
            TestHelper.race(r, () -> fo.onSubscribe(d));
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel2, this.description("cancel2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onError, this.description("onError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNext, this.description("onNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribe, this.description("onSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelRace, this.description("cancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_await() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::await, this.description("await"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCancelRace, this.description("onErrorCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCancelRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteCancelRace, this.description("onCompleteCancelRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorOnComplete, this.description("onErrorOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnError, this.description("onCompleteOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCompleteOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextCompleteOnError, this.description("onNextCompleteOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnError, this.description("cancelOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnComplete, this.description("cancelOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThenOnCompleteTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextThenOnCompleteTwice, this.description("onNextThenOnCompleteTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::getInterrupted, this.description("getInterrupted"), java.lang.InterruptedException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::completeAsync, this.description("completeAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getTimedOut() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::getTimedOut, this.description("getTimedOut"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnSubscribeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnSubscribeRace, this.description("cancelOnSubscribeRace"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FutureObserverTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FutureObserverTest();
        }

        @java.lang.Override
        public FutureObserverTest implementation() {
            return this.implementation;
        }
    }
}
