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

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleDoAfterTerminateTest extends RxJavaTest {

    private final int[] call = { 0 };

    private final Action afterTerminate = new Action() {

        @Override
        public void run() throws Exception {
            call[0]++;
        }
    };

    private final TestObserver<Integer> to = new TestObserver<>();

    @Test
    public void just() {
        Single.just(1).doAfterTerminate(afterTerminate).subscribeWith(to).assertResult(1);
        assertAfterTerminateCalledOnce();
    }

    @Test
    public void error() {
        Single.<Integer>error(new TestException()).doAfterTerminate(afterTerminate).subscribeWith(to).assertFailure(TestException.class);
        assertAfterTerminateCalledOnce();
    }

    @Test
    public void justConditional() {
        Single.just(1).doAfterTerminate(afterTerminate).filter(Functions.alwaysTrue()).subscribeWith(to).assertResult(1);
        assertAfterTerminateCalledOnce();
    }

    @Test
    public void errorConditional() {
        Single.<Integer>error(new TestException()).doAfterTerminate(afterTerminate).filter(Functions.alwaysTrue()).subscribeWith(to).assertFailure(TestException.class);
        assertAfterTerminateCalledOnce();
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.just(1).doAfterTerminate(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().singleOrError().doAfterTerminate(afterTerminate));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Single<Integer> m) throws Exception {
                return m.doAfterTerminate(afterTerminate);
            }
        });
    }

    private void assertAfterTerminateCalledOnce() {
        assertEquals(1, call[0]);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

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
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justConditional, this.description("justConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorConditional, this.description("errorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::actionThrows, this.description("actionThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private SingleDoAfterTerminateTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleDoAfterTerminateTest();
        }

        @java.lang.Override
        public SingleDoAfterTerminateTest implementation() {
            return this.implementation;
        }
    }
}
