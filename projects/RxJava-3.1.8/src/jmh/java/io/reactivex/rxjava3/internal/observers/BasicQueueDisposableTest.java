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

import org.junit.Test;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.RxJavaTest;

public class BasicQueueDisposableTest extends RxJavaTest {

    BasicQueueDisposable<Integer> q = new BasicQueueDisposable<Integer>() {

        @Override
        public boolean isDisposed() {
            return false;
        }

        @Override
        public void dispose() {
        }

        @Nullable
        @Override
        public Integer poll() throws Exception {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public int requestFusion(int mode) {
            return 0;
        }
    };

    @Test(expected = UnsupportedOperationException.class)
    public void offer() {
        q.offer(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void offer2() {
        q.offer(1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offer() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::offer, this.description("offer"), java.lang.UnsupportedOperationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offer2() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::offer2, this.description("offer2"), java.lang.UnsupportedOperationException.class);
        }

        private BasicQueueDisposableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BasicQueueDisposableTest();
        }

        @java.lang.Override
        public BasicQueueDisposableTest implementation() {
            return this.implementation;
        }
    }
}
