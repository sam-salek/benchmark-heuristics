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
package io.reactivex.rxjava3.single;

import static org.junit.Assert.assertTrue;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;

public class SingleNullTests extends RxJavaTest {

    Single<Integer> just1 = Single.just(1);

    Single<Integer> error = Single.error(new TestException());

    @Test
    public void ambIterableIteratorNull() {
        Single.amb(new Iterable<Single<Object>>() {

            @Override
            public Iterator<Single<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Single.amb(Arrays.asList(null, just1)).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambArrayOneIsNull() {
        Single.ambArray(null, just1).test().assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Single.concat(new Iterable<Single<Object>>() {

            @Override
            public Iterator<Single<Object>> iterator() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableOneIsNull() {
        Single.concat(Arrays.asList(just1, null)).blockingSubscribe();
    }

    @Test
    public void concatNull() throws Exception {
        int maxArgs = 4;
        @SuppressWarnings("rawtypes")
        Class<Single> clazz = Single.class;
        for (int argCount = 2; argCount <= maxArgs; argCount++) {
            for (int argNull = 1; argNull <= argCount; argNull++) {
                Class<?>[] params = new Class[argCount];
                Arrays.fill(params, SingleSource.class);
                Object[] values = new Object[argCount];
                Arrays.fill(values, just1);
                values[argNull - 1] = null;
                Method m = clazz.getMethod("concat", params);
                try {
                    m.invoke(null, values);
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void deferReturnsNull() {
        Single.defer(Functions.<Single<Object>>nullSupplier()).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void errorSupplierReturnsNull() {
        Single.error(Functions.<Throwable>nullSupplier()).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void fromCallableReturnsNull() {
        Single.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Single.fromFuture(f).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureTimedReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Single.fromFuture(f, 1, TimeUnit.SECONDS).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableIteratorNull() {
        Single.merge(new Iterable<Single<Object>>() {

            @Override
            public Iterator<Single<Object>> iterator() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableOneIsNull() {
        Single.merge(Arrays.asList(null, just1)).blockingSubscribe();
    }

    @Test
    public void mergeNull() throws Exception {
        int maxArgs = 4;
        @SuppressWarnings("rawtypes")
        Class<Single> clazz = Single.class;
        for (int argCount = 2; argCount <= maxArgs; argCount++) {
            for (int argNull = 1; argNull <= argCount; argNull++) {
                Class<?>[] params = new Class[argCount];
                Arrays.fill(params, SingleSource.class);
                Object[] values = new Object[argCount];
                Arrays.fill(values, just1);
                values[argNull - 1] = null;
                Method m = clazz.getMethod("merge", params);
                try {
                    m.invoke(null, values);
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void usingSingleSupplierReturnsNull() {
        Single.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Single<Object>>() {

            @Override
            public Single<Object> apply(Object d) {
                return null;
            }
        }, Functions.emptyConsumer()).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableIteratorNull() {
        Single.zip(new Iterable<Single<Object>>() {

            @Override
            public Iterator<Single<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableOneIsNull() {
        Single.zip(Arrays.asList(null, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableOneFunctionReturnsNull() {
        Single.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }).blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zipNull() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<Single> clazz = Single.class;
        for (int argCount = 3; argCount < 10; argCount++) {
            for (int argNull = 1; argNull <= argCount; argNull++) {
                Class<?>[] params = new Class[argCount + 1];
                Arrays.fill(params, SingleSource.class);
                Class<?> fniClass = Class.forName("io.reactivex.rxjava3.functions.Function" + argCount);
                params[argCount] = fniClass;
                Object[] values = new Object[argCount + 1];
                Arrays.fill(values, just1);
                values[argNull - 1] = null;
                values[argCount] = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { fniClass }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object o, Method m, Object[] a) throws Throwable {
                        return 1;
                    }
                });
                Method m = clazz.getMethod("zip", params);
                try {
                    m.invoke(null, values);
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
                values[argCount] = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { fniClass }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object o, Method m1, Object[] a) throws Throwable {
                        return null;
                    }
                });
                try {
                    ((Single<Object>) m.invoke(null, values)).blockingGet();
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
            }
            Class<?>[] params = new Class[argCount + 1];
            Arrays.fill(params, SingleSource.class);
            Class<?> fniClass = Class.forName("io.reactivex.rxjava3.functions.Function" + argCount);
            params[argCount] = fniClass;
            Object[] values = new Object[argCount + 1];
            Arrays.fill(values, just1);
            values[argCount] = null;
            Method m = clazz.getMethod("zip", params);
            try {
                m.invoke(null, values);
                Assert.fail("No exception for argCount " + argCount + " / zipper function ");
            } catch (InvocationTargetException ex) {
                if (!(ex.getCause() instanceof NullPointerException)) {
                    Assert.fail("Unexpected exception for argCount " + argCount + " / zipper function: " + ex);
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableTwoIsNull() {
        Single.zip(Arrays.asList(just1, null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipArrayOneIsNull() {
        Single.zipArray(new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, just1, null).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipArrayFunctionReturnsNull() {
        Single.zipArray(new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, just1, just1).blockingGet();
    }

    // **************************************************
    // Instance methods
    // **************************************************
    @Test(expected = NullPointerException.class)
    public void flatMapFunctionReturnsNull() {
        just1.flatMap(new Function<Integer, Single<Object>>() {

            @Override
            public Single<Object> apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapPublisherFunctionReturnsNull() {
        just1.flatMapPublisher(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void liftFunctionReturnsNull() {
        just1.lift(new SingleOperator<Object, Integer>() {

            @Override
            public SingleObserver<? super Integer> apply(SingleObserver<? super Object> observer) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void onErrorReturnsSupplierReturnsNull() {
        error.onErrorReturn(new Function<Throwable, Integer>() {

            @Override
            public Integer apply(Throwable t) throws Exception {
                return null;
            }
        }).blockingGet();
    }

    @Test
    public void onErrorResumeNextFunctionReturnsNull() {
        try {
            error.onErrorResumeNext(new Function<Throwable, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Throwable e) {
                    return null;
                }
            }).blockingGet();
        } catch (CompositeException ex) {
            assertTrue(ex.toString(), ex.getExceptions().get(1) instanceof NullPointerException);
        }
    }

    @Test(expected = NullPointerException.class)
    public void repeatWhenFunctionReturnsNull() {
        error.repeatWhen(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void retryWhenFunctionReturnsNull() {
        error.retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> e) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void subscribeOnSuccessNull() {
        just1.subscribe(null, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void zipWithFunctionReturnsNull() {
        just1.zipWith(just1, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingGet();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableIteratorNull, this.description("ambIterableIteratorNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambIterableOneIsNull, this.description("ambIterableOneIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ambArrayOneIsNull, this.description("ambArrayOneIsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableIteratorNull, this.description("concatIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::concatIterableOneIsNull, this.description("concatIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::concatNull, this.description("concatNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::deferReturnsNull, this.description("deferReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::errorSupplierReturnsNull, this.description("errorSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromCallableReturnsNull, this.description("fromCallableReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromFutureReturnsNull, this.description("fromFutureReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimedReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::fromFutureTimedReturnsNull, this.description("fromFutureTimedReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableIteratorNull, this.description("mergeIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::mergeIterableOneIsNull, this.description("mergeIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mergeNull, this.description("mergeNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingSingleSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::usingSingleSupplierReturnsNull, this.description("usingSingleSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableIteratorNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableIteratorNull, this.description("zipIterableIteratorNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableOneIsNull, this.description("zipIterableOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableOneFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableOneFunctionReturnsNull, this.description("zipIterableOneFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::zipNull, this.description("zipNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableTwoIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipIterableTwoIsNull, this.description("zipIterableTwoIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipArrayOneIsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipArrayOneIsNull, this.description("zipArrayOneIsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipArrayFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipArrayFunctionReturnsNull, this.description("zipArrayFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapFunctionReturnsNull, this.description("flatMapFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisherFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::flatMapPublisherFunctionReturnsNull, this.description("flatMapPublisherFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::liftFunctionReturnsNull, this.description("liftFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnsSupplierReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::onErrorReturnsSupplierReturnsNull, this.description("onErrorReturnsSupplierReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorResumeNextFunctionReturnsNull, this.description("onErrorResumeNextFunctionReturnsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::repeatWhenFunctionReturnsNull, this.description("repeatWhenFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::retryWhenFunctionReturnsNull, this.description("retryWhenFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnSuccessNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::subscribeOnSuccessNull, this.description("subscribeOnSuccessNull"), java.lang.NullPointerException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithFunctionReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::zipWithFunctionReturnsNull, this.description("zipWithFunctionReturnsNull"), java.lang.NullPointerException.class);
        }

        private SingleNullTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleNullTests();
        }

        @java.lang.Override
        public SingleNullTests implementation() {
            return this.implementation;
        }
    }
}
