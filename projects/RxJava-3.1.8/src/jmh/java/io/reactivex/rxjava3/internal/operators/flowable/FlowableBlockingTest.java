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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableBlockingTest extends RxJavaTest {

    @Test
    public void blockingFirst() {
        assertEquals(1, Flowable.range(1, 10).subscribeOn(Schedulers.computation()).blockingFirst().intValue());
    }

    @Test
    public void blockingFirstDefault() {
        assertEquals(1, Flowable.<Integer>empty().subscribeOn(Schedulers.computation()).blockingFirst(1).intValue());
    }

    @Test
    public void blockingSubscribeConsumer() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumer() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerBufferExceed() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumer() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumer() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer(), 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerBufferExceed() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer(), 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerError() {
        final List<Object> list = new ArrayList<>();
        TestException ex = new TestException();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerError() {
        final List<Object> list = new ArrayList<>();
        TestException ex = new TestException();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerAction() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerAction() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerActionBufferExceed() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(1000001);
            }
        };
        Flowable.range(1, 1000000).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 128);
        assertEquals(1000000 + 1, list.size());
    }

    @Test
    public void blockingSubscribeObserver() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void blockingSubscribeObserverError() {
        final List<Object> list = new ArrayList<>();
        final TestException ex = new TestException();
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test(expected = TestException.class)
    public void blockingForEachThrows() {
        Flowable.just(1).blockingForEach(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        });
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingFirstEmpty() {
        Flowable.empty().blockingFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingLastEmpty() {
        Flowable.empty().blockingLast();
    }

    @Test
    public void blockingFirstNormal() {
        assertEquals(1, Flowable.just(1, 2).blockingFirst(3).intValue());
    }

    @Test
    public void blockingLastNormal() {
        assertEquals(2, Flowable.just(1, 2).blockingLast(3).intValue());
    }

    @Test
    public void firstFgnoredCancelAndOnNext() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        });
        assertEquals(1, source.blockingFirst().intValue());
    }

    @Test
    public void firstIgnoredCancelAndOnError() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException());
                }
            });
            assertEquals(1, source.blockingFirst().intValue());
            TestHelper.assertUndeliverable(list, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test(expected = TestException.class)
    public void firstOnError() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onError(new TestException());
            }
        });
        source.blockingFirst();
    }

    @Test
    public void interrupt() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Thread.currentThread().interrupt();
        try {
            Flowable.just(1).blockingSubscribe(ts);
            ts.assertFailure(InterruptedException.class);
        } finally {
            // clear interrupted status just in case
            Thread.interrupted();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingSingleEmpty() {
        Flowable.empty().blockingSingle();
    }

    @Test
    public void onCompleteDelayed() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.empty().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe(ts);
        ts.assertResult();
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(FlowableBlockingSubscribe.class);
    }

    @Test
    public void disposeUpFront() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.cancel();
        Flowable.just(1).blockingSubscribe(ts);
        ts.assertEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void delayed() throws Exception {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        final Subscriber[] s = { null };
        Schedulers.single().scheduleDirect(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                ts.cancel();
                s[0].onNext(1);
            }
        }, 200, TimeUnit.MILLISECONDS);
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                s[0] = subscriber;
            }
        }.blockingSubscribe(ts);
        while (!ts.isCancelled()) {
            Thread.sleep(100);
        }
        ts.assertEmpty();
    }

    @Test
    public void blockinsSubscribeCancelAsync() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            final Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            final AtomicInteger c = new AtomicInteger(2);
            Schedulers.computation().scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    c.decrementAndGet();
                    while (c.get() != 0 && !pp.hasSubscribers()) {
                    }
                    TestHelper.race(r1, r2);
                }
            });
            c.decrementAndGet();
            while (c.get() != 0) {
            }
            pp.blockingSubscribe(ts);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingFirst, this.description("blockingFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstDefault() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingFirstDefault, this.description("blockingFirstDefault"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeConsumer, this.description("blockingSubscribeConsumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumer, this.description("boundedBlockingSubscribeConsumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerBufferExceed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerBufferExceed, this.description("boundedBlockingSubscribeConsumerBufferExceed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeConsumerConsumer, this.description("blockingSubscribeConsumerConsumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumer, this.description("boundedBlockingSubscribeConsumerConsumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerBufferExceed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumerBufferExceed, this.description("boundedBlockingSubscribeConsumerConsumerBufferExceed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeConsumerConsumerError, this.description("blockingSubscribeConsumerConsumerError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumerError, this.description("boundedBlockingSubscribeConsumerConsumerError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerAction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeConsumerConsumerAction, this.description("blockingSubscribeConsumerConsumerAction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerAction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumerAction, this.description("boundedBlockingSubscribeConsumerConsumerAction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerActionBufferExceed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumerActionBufferExceed, this.description("boundedBlockingSubscribeConsumerConsumerActionBufferExceed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem, this.description("boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserver() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeObserver, this.description("blockingSubscribeObserver"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserverError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingSubscribeObserverError, this.description("blockingSubscribeObserverError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingForEachThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::blockingForEachThrows, this.description("blockingForEachThrows"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::blockingFirstEmpty, this.description("blockingFirstEmpty"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::blockingLastEmpty, this.description("blockingLastEmpty"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingFirstNormal, this.description("blockingFirstNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockingLastNormal, this.description("blockingLastNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstFgnoredCancelAndOnNext() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstFgnoredCancelAndOnNext, this.description("firstFgnoredCancelAndOnNext"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstIgnoredCancelAndOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::firstIgnoredCancelAndOnError, this.description("firstIgnoredCancelAndOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::firstOnError, this.description("firstOnError"), io.reactivex.rxjava3.exceptions.TestException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interrupt, this.description("interrupt"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSingleEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::blockingSingleEmpty, this.description("blockingSingleEmpty"), java.util.NoSuchElementException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteDelayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteDelayed, this.description("onCompleteDelayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::utilityClass, this.description("utilityClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeUpFront() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposeUpFront, this.description("disposeUpFront"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delayed, this.description("delayed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockinsSubscribeCancelAsync() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::blockinsSubscribeCancelAsync, this.description("blockinsSubscribeCancelAsync"));
        }

        private FlowableBlockingTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableBlockingTest();
        }

        @java.lang.Override
        public FlowableBlockingTest implementation() {
            return this.implementation;
        }
    }
}
