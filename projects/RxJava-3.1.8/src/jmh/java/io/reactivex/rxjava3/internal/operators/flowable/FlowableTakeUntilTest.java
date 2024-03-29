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
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeUntilTest extends RxJavaTest {

    @Test
    public void takeUntil() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnNext("three");
        source.sendOnNext("four");
        source.sendOnCompleted();
        other.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(0)).onNext("four");
        verify(sSource, times(1)).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilSourceCompleted() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(sSource, never()).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilSourceError() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(sSource, never()).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilOtherError() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(result, times(0)).onComplete();
        verify(sSource, times(1)).cancel();
        verify(sOther, never()).cancel();
    }

    /**
     * If the 'other' onCompletes then we unsubscribe from the source and onComplete.
     */
    @Test
    public void takeUntilOtherCompleted() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnCompleted();
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onComplete();
        verify(sSource, times(1)).cancel();
        // unsubscribed since SafeSubscriber unsubscribes after onComplete
        verify(sOther, never()).cancel();
    }

    private static class TestObservable implements Publisher<String> {

        Subscriber<? super String> subscriber;

        Subscription upstream;

        TestObservable(Subscription s) {
            this.upstream = s;
        }

        /* used to simulate subscription */
        public void sendOnCompleted() {
            subscriber.onComplete();
        }

        /* used to simulate subscription */
        public void sendOnNext(String value) {
            subscriber.onNext(value);
        }

        /* used to simulate subscription */
        public void sendOnError(Throwable e) {
            subscriber.onError(e);
        }

        @Override
        public void subscribe(Subscriber<? super String> subscriber) {
            this.subscriber = subscriber;
            subscriber.onSubscribe(upstream);
        }
    }

    @Test
    public void untilFires() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        ts.assertValue(1);
        until.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        source.onComplete();
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void downstreamUnsubscribes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).take(1).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void backpressure() {
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.range(1, 10).takeUntil(until).subscribe(ts);
        assertTrue(until.hasSubscribers());
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        until.onNext(5);
        ts.assertComplete();
        ts.assertNoErrors();
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().takeUntil(Flowable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> c) throws Exception {
                return c.takeUntil(Flowable.never());
            }
        });
    }

    @Test
    public void untilPublisherMainSuccess() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onNext(1);
        main.onNext(2);
        main.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult(1, 2);
    }

    @Test
    public void untilPublisherMainComplete() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onError(new TestException());
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onNext(1);
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onError(new TestException());
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        ts.cancel();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntil() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntil, this.description("takeUntil"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilSourceCompleted, this.description("takeUntilSourceCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilSourceError, this.description("takeUntilSourceError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilOtherError, this.description("takeUntilOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::takeUntilOtherCompleted, this.description("takeUntilOtherCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFires() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFires, this.description("untilFires"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainCompletes, this.description("mainCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_downstreamUnsubscribes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::downstreamUnsubscribes, this.description("downstreamUnsubscribes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressure, this.description("backpressure"));
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
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainSuccess, this.description("untilPublisherMainSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainComplete, this.description("untilPublisherMainComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherMainError, this.description("untilPublisherMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherOnNext, this.description("untilPublisherOtherOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherOnComplete, this.description("untilPublisherOtherOnComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherOtherError, this.description("untilPublisherOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilPublisherDispose, this.description("untilPublisherDispose"));
        }

        private FlowableTakeUntilTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableTakeUntilTest();
        }

        @java.lang.Override
        public FlowableTakeUntilTest implementation() {
            return this.implementation;
        }
    }
}
