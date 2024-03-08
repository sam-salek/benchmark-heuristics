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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableTakeLastTest extends RxJavaTest {

    @Test
    public void takeLastEmpty() {
        Flowable<String> w = Flowable.empty();
        Flowable<String> take = w.takeLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLast1() {
        Flowable<String> w = Flowable.just("one", "two", "three");
        Flowable<String> take = w.takeLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        take.subscribe(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLast2() {
        Flowable<String> w = Flowable.just("one");
        Flowable<String> take = w.takeLast(10);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLastWithZeroCount() {
        Flowable<String> w = Flowable.just("one");
        Flowable<String> take = w.takeLast(0);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, never()).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeLastWithNegativeCount() {
        Flowable.just("one").takeLast(-1);
    }

    @Test
    public void backpressure1() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 100000).takeLast(1).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertValue(100000);
    }

    @Test
    public void backpressure2() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 100000).takeLast(Flowable.bufferSize() * 4).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 4, ts.values().size());
    }

    private Function<Integer, Integer> newSlowProcessor() {
        return new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer i) {
                if (c++ < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                return i;
            }
        };
    }

    @Test
    public void issue1522() {
        // https://github.com/ReactiveX/RxJava/issues/1522
        assertEquals(0, Flowable.empty().count().toFlowable().filter(new Predicate<Long>() {

            @Override
            public boolean test(Long v) {
                return false;
            }
        }).toList().blockingGet().size());
    }

    @Test
    public void ignoreRequest1() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(Long.MAX_VALUE);
            }
        });
    }

    @Test
    public void ignoreRequest2() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
    }

    @Test
    public void ignoreRequest3() {
        // If `takeLast` does not ignore `request` properly, it will enter an infinite loop.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(Long.MAX_VALUE);
            }
        });
    }

    @Test
    public void ignoreRequest4() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
    }

    @Test
    public void unsubscribeTakesEffectEarlyOnFastPath() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                count.incrementAndGet();
                cancel();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    public void requestOverflow() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 100).takeLast(50).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                list.add(t);
                request(Long.MAX_VALUE - 1);
            }
        });
        assertEquals(50, list.size());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 10).takeLast(5));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeLast(5);
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).takeLast(5).test().assertFailure(TestException.class);
    }

    @Test
    public void takeLastTake() {
        Flowable.range(1, 10).takeLast(5).take(2).test().assertResult(6, 7);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().takeLast(2));
    }

    @Test
    public void cancelThenRequest() {
        Flowable.never().takeLast(2).subscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onNext(@NonNull Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                s.cancel();
                s.request(1);
            }
        });
    }

    @Test
    public void noRequestEmpty() {
        Flowable.empty().takeLast(2).test(0L).assertResult();
    }

    @Test
    public void moreValuesRemainingThanRequested() {
        Flowable.range(1, 4).takeLast(3).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 3).requestMore(2).assertResult(2, 3, 4);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastEmpty, this.description("takeLastEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLast1, this.description("takeLast1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLast2, this.description("takeLast2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithZeroCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastWithZeroCount, this.description("takeLastWithZeroCount"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithNegativeCount() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::takeLastWithNegativeCount, this.description("takeLastWithNegativeCount"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure1, this.description("backpressure1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure2, this.description("backpressure2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1522() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1522, this.description("issue1522"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreRequest1, this.description("ignoreRequest1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreRequest2, this.description("ignoreRequest2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreRequest3, this.description("ignoreRequest3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoreRequest4, this.description("ignoreRequest4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeTakesEffectEarlyOnFastPath() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::unsubscribeTakesEffectEarlyOnFastPath, this.description("unsubscribeTakesEffectEarlyOnFastPath"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::requestOverflow, this.description("requestOverflow"));
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeLastTake, this.description("takeLastTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelThenRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelThenRequest, this.description("cancelThenRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noRequestEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noRequestEmpty, this.description("noRequestEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_moreValuesRemainingThanRequested() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::moreValuesRemainingThanRequested, this.description("moreValuesRemainingThanRequested"));
        }

        private FlowableTakeLastTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableTakeLastTest();
        }

        @java.lang.Override
        public FlowableTakeLastTest implementation() {
            return this.implementation;
        }
    }
}