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

import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.CompletableSubject;

public class CompletableToFutureTest extends RxJavaTest {

    @Test
    public void empty() throws Exception {
        assertNull(Completable.complete().subscribeOn(Schedulers.computation()).toFuture().get());
    }

    @Test
    public void error() throws InterruptedException {
        try {
            Completable.error(new TestException()).subscribeOn(Schedulers.computation()).toFuture().get();
            fail("Should have thrown!");
        } catch (ExecutionException ex) {
            assertTrue("" + ex.getCause(), ex.getCause() instanceof TestException);
        }
    }

    @Test
    public void cancel() {
        CompletableSubject cs = CompletableSubject.create();
        Future<Void> f = cs.toFuture();
        assertTrue(cs.hasObservers());
        f.cancel(true);
        assertFalse(cs.hasObservers());
    }

    @Test
    public void cancel2() {
        CompletableSubject cs = CompletableSubject.create();
        Future<Void> f = cs.toFuture();
        assertTrue(cs.hasObservers());
        f.cancel(false);
        assertFalse(cs.hasObservers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel, this.description("cancel"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancel2, this.description("cancel2"));
        }

        private CompletableToFutureTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableToFutureTest();
        }

        @java.lang.Override
        public CompletableToFutureTest implementation() {
            return this.implementation;
        }
    }
}
