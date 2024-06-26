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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableStageSubscriberOrErrorTest extends RxJavaTest {

    @Test
    public void firstJust() throws Exception {
        Integer v = Flowable.just(1).firstOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void firstEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().firstOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void firstCancels() throws Exception {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        Integer v = source.firstOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void firstCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void firstCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void firstCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void firstError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).firstOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void firstSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.firstOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void firstDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                }
            }.firstOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @Test
    public void singleJust() throws Exception {
        Integer v = Flowable.just(1).singleOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void singleEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().singleOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void singleTooManyCancels() throws Exception {
        ReplayProcessor<Integer> source = ReplayProcessor.create();
        source.onNext(1);
        source.onNext(2);
        TestHelper.assertError(source.singleOrErrorStage().toCompletableFuture(), IllegalArgumentException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void singleCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void singleCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void singleCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void singleError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).singleOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void singleSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.singleOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void singleDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                }
            }.singleOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @Test
    public void lastJust() throws Exception {
        Integer v = Flowable.just(1).lastOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void lastRange() throws Exception {
        Integer v = Flowable.range(1, 5).lastOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 5, v);
    }

    @Test
    public void lastEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().lastOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void lastCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void lastCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void lastCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void lastError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).lastOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void lastSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.lastOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void lastDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                }
            }.lastOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstJust, this.description("firstJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstEmpty, this.description("firstEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstCancels, this.description("firstCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableFutureCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstCompletableFutureCancels, this.description("firstCompletableFutureCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstCompletableManualCompleteCancels, this.description("firstCompletableManualCompleteCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstCompletableManualCompleteExceptionallyCancels, this.description("firstCompletableManualCompleteExceptionallyCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstError, this.description("firstError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstSourceIgnoresCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstSourceIgnoresCancel, this.description("firstSourceIgnoresCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstDoubleOnSubscribe, this.description("firstDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleJust, this.description("singleJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleEmpty, this.description("singleEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleTooManyCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleTooManyCancels, this.description("singleTooManyCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableFutureCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleCompletableFutureCancels, this.description("singleCompletableFutureCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleCompletableManualCompleteCancels, this.description("singleCompletableManualCompleteCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleCompletableManualCompleteExceptionallyCancels, this.description("singleCompletableManualCompleteExceptionallyCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleError, this.description("singleError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceIgnoresCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleSourceIgnoresCancel, this.description("singleSourceIgnoresCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::singleDoubleOnSubscribe, this.description("singleDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastJust() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastJust, this.description("lastJust"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastRange() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastRange, this.description("lastRange"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastEmpty, this.description("lastEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableFutureCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastCompletableFutureCancels, this.description("lastCompletableFutureCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastCompletableManualCompleteCancels, this.description("lastCompletableManualCompleteCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastCompletableManualCompleteExceptionallyCancels, this.description("lastCompletableManualCompleteExceptionallyCancels"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastError, this.description("lastError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastSourceIgnoresCancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastSourceIgnoresCancel, this.description("lastSourceIgnoresCancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lastDoubleOnSubscribe, this.description("lastDoubleOnSubscribe"));
        }

        private FlowableStageSubscriberOrErrorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableStageSubscriberOrErrorTest();
        }

        @java.lang.Override
        public FlowableStageSubscriberOrErrorTest implementation() {
            return this.implementation;
        }
    }
}
