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
package io.reactivex.rxjava3.internal.operators.flowable;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFromObservableTest extends RxJavaTest {

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).toFlowable(BackpressureStrategy.MISSING));
    }

    @Test
    public void error() {
        Observable.error(new TestException()).toFlowable(BackpressureStrategy.MISSING).test().assertFailure(TestException.class);
    }

    @Test
    public void all() {
        for (BackpressureStrategy mode : BackpressureStrategy.values()) {
            Flowable.fromObservable(Observable.range(1, 5), mode).test().withTag("mode: " + mode).assertResult(1, 2, 3, 4, 5);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_all() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::all, this.description("all"));
        }

        private FlowableFromObservableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableFromObservableTest();
        }

        @java.lang.Override
        public FlowableFromObservableTest implementation() {
            return this.implementation;
        }
    }
}
