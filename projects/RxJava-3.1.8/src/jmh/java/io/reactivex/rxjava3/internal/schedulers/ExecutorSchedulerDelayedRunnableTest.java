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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.schedulers.ExecutorScheduler.DelayedRunnable;
import io.reactivex.rxjava3.testsupport.SuppressUndeliverable;

public class ExecutorSchedulerDelayedRunnableTest extends RxJavaTest {

    @Test(expected = TestException.class)
    @SuppressUndeliverable
    public void delayedRunnableCrash() {
        DelayedRunnable dl = new DelayedRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        dl.run();
    }

    @Test
    public void dispose() {
        final AtomicInteger count = new AtomicInteger();
        DelayedRunnable dl = new DelayedRunnable(new Runnable() {

            @Override
            public void run() {
                count.incrementAndGet();
            }
        });
        dl.dispose();
        dl.dispose();
        dl.run();
        assertEquals(0, count.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedRunnableCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::delayedRunnableCrash, this.description("delayedRunnableCrash"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        private ExecutorSchedulerDelayedRunnableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ExecutorSchedulerDelayedRunnableTest();
        }

        @java.lang.Override
        public ExecutorSchedulerDelayedRunnableTest implementation() {
            return this.implementation;
        }
    }
}
