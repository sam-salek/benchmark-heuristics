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

import static org.mockito.Mockito.*;
import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;

public class CompletableConcatDelayErrorTest {

    @Test
    public void normalIterable() throws Throwable {
        Action action1 = mock(Action.class);
        Action action2 = mock(Action.class);
        Completable.concatDelayError(Arrays.asList(Completable.fromAction(action1), Completable.error(new TestException()), Completable.fromAction(action2))).test().assertFailure(TestException.class);
        verify(action1).run();
        verify(action2).run();
    }

    @Test
    public void normalPublisher() throws Throwable {
        Action action1 = mock(Action.class);
        Action action2 = mock(Action.class);
        Completable.concatDelayError(Flowable.fromArray(Completable.fromAction(action1), Completable.error(new TestException()), Completable.fromAction(action2))).test().assertFailure(TestException.class);
        verify(action1).run();
        verify(action2).run();
    }

    @Test
    public void normalPublisherPrefetch() throws Throwable {
        Action action1 = mock(Action.class);
        Action action2 = mock(Action.class);
        Completable.concatDelayError(Flowable.fromArray(Completable.fromAction(action1), Completable.error(new TestException()), Completable.fromAction(action2)), 1).test().assertFailure(TestException.class);
        verify(action1).run();
        verify(action2).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalIterable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalIterable, this.description("normalIterable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalPublisher, this.description("normalPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisherPrefetch() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalPublisherPrefetch, this.description("normalPublisherPrefetch"));
        }

        private CompletableConcatDelayErrorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableConcatDelayErrorTest();
        }

        @java.lang.Override
        public CompletableConcatDelayErrorTest implementation() {
            return this.implementation;
        }
    }
}
