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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeTakeUntilTest extends RxJavaTest {

    @Test
    public void normalPublisher() {
        Maybe.just(1).takeUntil(Flowable.never()).test().assertResult(1);
    }

    @Test
    public void normalMaybe() {
        Maybe.just(1).takeUntil(Maybe.never()).test().assertResult(1);
    }

    @Test
    public void untilFirstPublisher() {
        Maybe.just(1).takeUntil(Flowable.just("one")).test().assertResult();
    }

    @Test
    public void untilFirstMaybe() {
        Maybe.just(1).takeUntil(Maybe.just("one")).test().assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().takeUntil(Maybe.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.takeUntil(Maybe.never());
            }
        });
    }

    @Test
    public void mainErrors() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherErrors() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void otherCompletes() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
            final TestException ex1 = new TestException();
            final TestException ex2 = new TestException();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp1.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp2.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult();
        }
    }

    @Test
    public void untilMaybeMainSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void untilMaybeMainComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeMainError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilMaybeOtherSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeOtherComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeOtherError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilMaybeDispose() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertEmpty();
    }

    @Test
    public void untilPublisherMainSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void untilPublisherMainComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onNext(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalPublisher, this.description("normalPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normalMaybe, this.description("normalMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFirstPublisher() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFirstPublisher, this.description("untilFirstPublisher"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFirstMaybe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilFirstMaybe, this.description("untilFirstMaybe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposed, this.description("disposed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainErrors, this.description("mainErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherErrors, this.description("otherErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mainCompletes, this.description("mainCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompletes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::otherCompletes, this.description("otherCompletes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorRace, this.description("onErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteRace, this.description("onCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeMainSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeMainSuccess, this.description("untilMaybeMainSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeMainComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeMainComplete, this.description("untilMaybeMainComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeMainError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeMainError, this.description("untilMaybeMainError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeOtherSuccess, this.description("untilMaybeOtherSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeOtherComplete, this.description("untilMaybeOtherComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeOtherError, this.description("untilMaybeOtherError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::untilMaybeDispose, this.description("untilMaybeDispose"));
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

        private MaybeTakeUntilTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeTakeUntilTest();
        }

        @java.lang.Override
        public MaybeTakeUntilTest implementation() {
            return this.implementation;
        }
    }
}
