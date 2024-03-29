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

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeDoOnTerminateTest extends RxJavaTest {

    @Test
    public void doOnTerminateSuccess() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Maybe.just(1).doOnTerminate(new Action() {

            @Override
            public void run() {
                atomicBoolean.set(true);
            }
        }).test().assertResult(1);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Maybe.error(new TestException()).doOnTerminate(new Action() {

            @Override
            public void run() {
                atomicBoolean.set(true);
            }
        }).test().assertFailure(TestException.class);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void doOnTerminateComplete() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Maybe.empty().doOnTerminate(new Action() {

            @Override
            public void run() {
                atomicBoolean.set(true);
            }
        }).test().assertResult();
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void doOnTerminateSuccessCrash() {
        Maybe.just(1).doOnTerminate(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnTerminateErrorCrash() {
        TestObserverEx<Object> to = Maybe.error(new TestException("Outer")).doOnTerminate(new Action() {

            @Override
            public void run() {
                throw new TestException("Inner");
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnTerminateCompleteCrash() {
        Maybe.empty().doOnTerminate(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateSuccess, this.description("doOnTerminateSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateError, this.description("doOnTerminateError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateComplete, this.description("doOnTerminateComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateSuccessCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateSuccessCrash, this.description("doOnTerminateSuccessCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateErrorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateErrorCrash, this.description("doOnTerminateErrorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateCompleteCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnTerminateCompleteCrash, this.description("doOnTerminateCompleteCrash"));
        }

        private MaybeDoOnTerminateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeDoOnTerminateTest();
        }

        @java.lang.Override
        public MaybeDoOnTerminateTest implementation() {
            return this.implementation;
        }
    }
}
