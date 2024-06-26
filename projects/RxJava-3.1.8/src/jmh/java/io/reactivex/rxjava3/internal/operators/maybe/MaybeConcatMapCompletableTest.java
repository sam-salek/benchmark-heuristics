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

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeConcatMapCompletableTest extends RxJavaTest {

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.just(1).concatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void mapperThrows() {
        Maybe.just(1).concatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperReturnsNull() {
        Maybe.just(1).concatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperThrows, this.description("mapperThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mapperReturnsNull, this.description("mapperReturnsNull"));
        }

        private MaybeConcatMapCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeConcatMapCompletableTest();
        }

        @java.lang.Override
        public MaybeConcatMapCompletableTest implementation() {
            return this.implementation;
        }
    }
}
