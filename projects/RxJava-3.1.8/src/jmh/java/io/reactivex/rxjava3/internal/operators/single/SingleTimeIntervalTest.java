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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleTimeIntervalTest {

    @Test
    public void just() {
        Single.just(1).timestamp().test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void error() {
        Single.error(new TestException()).timestamp().test().assertFailure(TestException.class);
    }

    @Test
    public void justSeconds() {
        Single.just(1).timestamp(TimeUnit.SECONDS).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justScheduler() {
        Single.just(1).timestamp(Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justSecondsScheduler() {
        Single.just(1).timestamp(TimeUnit.SECONDS, Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(m -> m.timestamp());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(SingleSubject.create().timestamp());
    }

    @Test
    public void timeInfo() {
        TestScheduler scheduler = new TestScheduler();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestObserver<Timed<Integer>> to = ss.timestamp(scheduler).test();
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        ss.onSuccess(1);
        to.assertResult(new Timed<>(1, 1000L, TimeUnit.MILLISECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSeconds() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justSeconds, this.description("justSeconds"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justScheduler, this.description("justScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSecondsScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justSecondsScheduler, this.description("justSecondsScheduler"));
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
        public void benchmark_timeInfo() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::timeInfo, this.description("timeInfo"));
        }

        private SingleTimeIntervalTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleTimeIntervalTest();
        }

        @java.lang.Override
        public SingleTimeIntervalTest implementation() {
            return this.implementation;
        }
    }
}
