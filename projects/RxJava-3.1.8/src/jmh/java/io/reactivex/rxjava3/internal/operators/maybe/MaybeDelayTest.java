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
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDelayTest extends RxJavaTest {

    @Test
    public void success() {
        Maybe.just(1).delay(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).delay(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void complete() {
        Maybe.empty().delay(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void disposeDuringDelay() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Integer> to = Maybe.just(1).delay(100, TimeUnit.MILLISECONDS, scheduler).test();
        to.dispose();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertEmpty();
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Integer> to = pp.singleElement().delay(100, TimeUnit.MILLISECONDS).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().delay(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.delay(100, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Test
    public void delayedErrorOnSuccess() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<Integer> observer = Maybe.just(1).delay(5, TimeUnit.SECONDS, scheduler, true).test();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        observer.assertNoValues();
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
        observer.assertValue(1);
    }

    @Test
    public void delayedErrorOnError() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<?> observer = Maybe.error(new TestException()).delay(5, TimeUnit.SECONDS, scheduler, true).test();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        observer.assertNoErrors();
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
        observer.assertError(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::success, this.description("success"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::complete, this.description("complete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeDuringDelay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeDuringDelay, this.description("disposeDuringDelay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isDisposed, this.description("isDisposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedErrorOnSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayedErrorOnSuccess, this.description("delayedErrorOnSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedErrorOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayedErrorOnError, this.description("delayedErrorOnError"));
        }

        private MaybeDelayTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDelayTest();
        }

        @java.lang.Override
        public MaybeDelayTest implementation() {
            return this.implementation;
        }
    }
}
