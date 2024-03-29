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
package io.reactivex.rxjava3.internal.operators.completable;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableBlockingSubscribeTest {

    @Test
    public void noArgComplete() {
        Completable.complete().blockingSubscribe();
    }

    @Test
    public void noArgCompleteAsync() {
        Completable.complete().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe();
    }

    @Test
    public void noArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Completable.error(new TestException()).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void noArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Completable.error(new TestException()).delay(100, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void oneArgComplete() throws Throwable {
        Action action = mock(Action.class);
        Completable.complete().blockingSubscribe(action);
        verify(action).run();
    }

    @Test
    public void oneArgCompleteAsync() throws Throwable {
        Action action = mock(Action.class);
        Completable.complete().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(action);
        verify(action).run();
    }

    @Test
    public void oneArgCompleteFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            doThrow(new TestException()).when(action).run();
            Completable.complete().blockingSubscribe(action);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(action).run();
        });
    }

    @Test
    public void oneArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            Completable.error(new TestException()).blockingSubscribe(action);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(action, never()).run();
        });
    }

    @Test
    public void oneArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            Completable.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe(action);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(action, never()).run();
        });
    }

    @Test
    public void twoArgComplete() throws Throwable {
        Action action = mock(Action.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Completable.complete().blockingSubscribe(action, consumer);
        verify(action).run();
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgCompleteAsync() throws Throwable {
        Action action = mock(Action.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Completable.complete().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(action, consumer);
        verify(action).run();
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgCompleteFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            doThrow(new TestException()).when(action).run();
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Completable.complete().blockingSubscribe(action, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(action).run();
            verify(consumer, never()).accept(any());
        });
    }

    @Test
    public void twoArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Completable.error(new TestException()).blockingSubscribe(action, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(action, never()).run();
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Completable.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe(action, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(action, never()).run();
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action action = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            doThrow(new TestException()).when(consumer).accept(any());
            Completable.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe(action, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(action, never()).run();
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            Action action = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Thread.currentThread().interrupt();
            Completable.never().doOnDispose(onDispose).blockingSubscribe(action, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            verify(action, never()).run();
            verify(consumer).accept(any(InterruptedException.class));
        });
    }

    @Test
    public void observerComplete() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.complete().blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerCompleteAsync() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.complete().delay(50, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerError() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.error(new TestException()).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerErrorAsync() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation(), true).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerDispose() throws Throwable {
        Action onDispose = mock(Action.class);
        TestObserver<Void> to = new TestObserver<>();
        to.dispose();
        Completable.never().doOnDispose(onDispose).blockingSubscribe(to);
        to.assertEmpty();
        verify(onDispose).run();
    }

    @Test
    public void ovserverInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            TestObserver<Void> to = new TestObserver<>();
            Thread.currentThread().interrupt();
            Completable.never().doOnDispose(onDispose).blockingSubscribe(to);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            to.assertFailure(InterruptedException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgComplete, this.description("noArgComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgCompleteAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgCompleteAsync, this.description("noArgCompleteAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgError, this.description("noArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noArgErrorAsync, this.description("noArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgComplete, this.description("oneArgComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgCompleteAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgCompleteAsync, this.description("oneArgCompleteAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgCompleteFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgCompleteFails, this.description("oneArgCompleteFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgError, this.description("oneArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::oneArgErrorAsync, this.description("oneArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgComplete, this.description("twoArgComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgCompleteAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgCompleteAsync, this.description("twoArgCompleteAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgCompleteFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgCompleteFails, this.description("twoArgCompleteFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgError, this.description("twoArgError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgErrorAsync, this.description("twoArgErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorFails() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgErrorFails, this.description("twoArgErrorFails"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::twoArgInterrupted, this.description("twoArgInterrupted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerComplete, this.description("observerComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCompleteAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerCompleteAsync, this.description("observerCompleteAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerError, this.description("observerError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerErrorAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerErrorAsync, this.description("observerErrorAsync"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::observerDispose, this.description("observerDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ovserverInterrupted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ovserverInterrupted, this.description("ovserverInterrupted"));
        }

        private CompletableBlockingSubscribeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableBlockingSubscribeTest();
        }

        @java.lang.Override
        public CompletableBlockingSubscribeTest implementation() {
            return this.implementation;
        }
    }
}
