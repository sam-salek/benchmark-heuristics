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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableAnyTest extends RxJavaTest {

    @Test
    public void anyWithTwoItems() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithTwoItems() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithOneItem() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithOneItem() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmpty() {
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithEmpty() {
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate1() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void exists1() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate2() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmptyAndPredicate() {
        // If the source is empty, always output false.
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void withFollowingFirst() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Single<Boolean> anyEven = f.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        });
        assertTrue(anyEven.blockingGet());
    }

    @Test
    public void issue1935NoUnsubscribeDownstream() {
        Flowable<Integer> source = Flowable.just(1).isEmpty().flatMapPublisher(new Function<Boolean, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Boolean t1) {
                return Flowable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void backpressureIfOneRequestedOneShouldBeDelivered() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoErrors();
        to.assertComplete();
        to.assertValue(true);
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessage() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Flowable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
        // FIXME value as last cause?
        // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void anyWithTwoItemsFlowable() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithTwoItemsFlowable() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Flowable<Boolean> flowable = w.isEmpty().toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithOneItemFlowable() {
        Flowable<Integer> w = Flowable.just(1);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithOneItemFlowable() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmptyFlowable() {
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithEmptyFlowable() {
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.isEmpty().toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate1Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void exists1Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate2Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithEmptyAndPredicateFlowable() {
        // If the source is empty, always output false.
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void withFollowingFirstFlowable() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Flowable<Boolean> anyEven = f.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        }).toFlowable();
        assertTrue(anyEven.blockingFirst());
    }

    @Test
    public void issue1935NoUnsubscribeDownstreamFlowable() {
        Flowable<Integer> source = Flowable.just(1).isEmpty().flatMapPublisher(new Function<Boolean, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Boolean t1) {
                return Flowable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable() {
        TestSubscriber<Boolean> ts = new TestSubscriber<>(0L);
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        }).toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void backpressureIfOneRequestedOneShouldBeDeliveredFlowable() {
        TestSubscriberEx<Boolean> ts = new TestSubscriberEx<>(1L);
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable().subscribe(ts);
        ts.assertTerminated();
        ts.assertNoErrors();
        ts.assertComplete();
        ts.assertValue(true);
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessageFlowable() {
        TestSubscriberEx<Boolean> ts = new TestSubscriberEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Flowable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).toFlowable().subscribe(ts);
        ts.assertTerminated();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(ex);
        // FIXME value as last cause?
        // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).any(Functions.alwaysTrue()).toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).any(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Boolean>>() {

            @Override
            public Publisher<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.any(Functions.alwaysTrue()).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.any(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void predicateThrowsSuppressOthers() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new IOException());
                    subscriber.onComplete();
                }
            }.any(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceSingle() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(1);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            }.any(Functions.alwaysTrue()).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithTwoItems() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithTwoItems, this.description("anyWithTwoItems"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItems() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithTwoItems, this.description("isEmptyWithTwoItems"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithOneItem, this.description("anyWithOneItem"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithOneItem, this.description("isEmptyWithOneItem"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithEmpty, this.description("anyWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithEmpty, this.description("isEmptyWithEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithPredicate1, this.description("anyWithPredicate1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::exists1, this.description("exists1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithPredicate2, this.description("anyWithPredicate2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicate() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithEmptyAndPredicate, this.description("anyWithEmptyAndPredicate"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFollowingFirst, this.description("withFollowingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstream() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1935NoUnsubscribeDownstream, this.description("issue1935NoUnsubscribeDownstream"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfOneRequestedOneShouldBeDelivered() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureIfOneRequestedOneShouldBeDelivered, this.description("backpressureIfOneRequestedOneShouldBeDelivered"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessage() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::predicateThrowsExceptionAndValueInCauseMessage, this.description("predicateThrowsExceptionAndValueInCauseMessage"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithTwoItemsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithTwoItemsFlowable, this.description("anyWithTwoItemsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItemsFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithTwoItemsFlowable, this.description("isEmptyWithTwoItemsFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItemFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithOneItemFlowable, this.description("anyWithOneItemFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItemFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithOneItemFlowable, this.description("isEmptyWithOneItemFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithEmptyFlowable, this.description("anyWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmptyFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::isEmptyWithEmptyFlowable, this.description("isEmptyWithEmptyFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1Flowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithPredicate1Flowable, this.description("anyWithPredicate1Flowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1Flowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::exists1Flowable, this.description("exists1Flowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2Flowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithPredicate2Flowable, this.description("anyWithPredicate2Flowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicateFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::anyWithEmptyAndPredicateFlowable, this.description("anyWithEmptyAndPredicateFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirstFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::withFollowingFirstFlowable, this.description("withFollowingFirstFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstreamFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::issue1935NoUnsubscribeDownstreamFlowable, this.description("issue1935NoUnsubscribeDownstreamFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable, this.description("backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfOneRequestedOneShouldBeDeliveredFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureIfOneRequestedOneShouldBeDeliveredFlowable, this.description("backpressureIfOneRequestedOneShouldBeDeliveredFlowable"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessageFlowable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::predicateThrowsExceptionAndValueInCauseMessageFlowable, this.description("predicateThrowsExceptionAndValueInCauseMessageFlowable"));
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
        public void benchmark_predicateThrowsSuppressOthers() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::predicateThrowsSuppressOthers, this.description("predicateThrowsSuppressOthers"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSingle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSourceSingle, this.description("badSourceSingle"));
        }

        private FlowableAnyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableAnyTest();
        }

        @java.lang.Override
        public FlowableAnyTest implementation() {
            return this.implementation;
        }
    }
}
