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

import static org.junit.Assert.*;
import java.util.NoSuchElementException;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.MaybeSubject;

public class SingleFromMaybeTest extends RxJavaTest {

    @Test
    public void success() {
        Single.fromMaybe(Maybe.just(1).hide()).test().assertResult(1);
    }

    @Test
    public void empty() {
        Single.fromMaybe(Maybe.empty().hide()).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void emptyDefault() {
        Single.fromMaybe(Maybe.empty().hide(), 1).test().assertResult(1);
    }

    @Test
    public void error() {
        Single.fromMaybe(Maybe.error(new TestException()).hide()).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelComposes() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Integer> to = Single.fromMaybe(ms).test();
        to.assertEmpty();
        assertTrue(ms.hasObservers());
        to.dispose();
        assertFalse(ms.hasObservers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::success, this.description("success"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyDefault, this.description("emptyDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelComposes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelComposes, this.description("cancelComposes"));
        }

        private SingleFromMaybeTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleFromMaybeTest();
        }

        @java.lang.Override
        public SingleFromMaybeTest implementation() {
            return this.implementation;
        }
    }
}
