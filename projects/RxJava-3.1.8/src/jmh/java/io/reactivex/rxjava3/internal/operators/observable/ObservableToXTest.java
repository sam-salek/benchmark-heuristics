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
package io.reactivex.rxjava3.internal.operators.observable;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class ObservableToXTest extends RxJavaTest {

    @Test
    public void toFlowableBuffer() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.BUFFER).test(2L).assertValues(1, 2).assertNoErrors().assertNotComplete();
    }

    @Test
    public void toFlowableDrop() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.DROP).test(1).assertResult(1);
    }

    @Test
    public void toFlowableLatest() {
        TestSubscriber<Integer> ts = Observable.range(1, 5).toFlowable(BackpressureStrategy.LATEST).test(0);
        ts.request(1);
        ts.assertResult(5);
    }

    @Test
    public void toFlowableError1() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.ERROR).test(1).assertFailure(MissingBackpressureException.class, 1);
    }

    @Test
    public void toFlowableError2() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.ERROR).test(5).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void toFlowableMissing() {
        TestSubscriber<Integer> ts = Observable.range(1, 5).toFlowable(BackpressureStrategy.MISSING).test(0);
        ts.request(2);
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableBuffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableBuffer, this.description("toFlowableBuffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableDrop() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableDrop, this.description("toFlowableDrop"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableLatest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableLatest, this.description("toFlowableLatest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableError1, this.description("toFlowableError1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableError2, this.description("toFlowableError2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableMissing() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toFlowableMissing, this.description("toFlowableMissing"));
        }

        private ObservableToXTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ObservableToXTest();
        }

        @java.lang.Override
        public ObservableToXTest implementation() {
            return this.implementation;
        }
    }
}
