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

import static org.junit.Assert.assertSame;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeToCompletableTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void source() {
        Maybe<Integer> source = Maybe.just(1);
        assertSame(source, ((HasUpstreamMaybeSource<Integer>) source.ignoreElement().toMaybe()).source());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.never().ignoreElement().toMaybe());
    }

    @Test
    public void successToComplete() {
        Maybe.just(1).ignoreElement().test().assertResult();
    }

    @Test
    public void doubleSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToCompletable(new Function<Maybe<Object>, CompletableSource>() {

            @Override
            public CompletableSource apply(Maybe<Object> m) throws Exception {
                return m.ignoreElement();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::source, this.description("source"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successToComplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::successToComplete, this.description("successToComplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleSubscribe, this.description("doubleSubscribe"));
        }

        private MaybeToCompletableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeToCompletableTest();
        }

        @java.lang.Override
        public MaybeToCompletableTest implementation() {
            return this.implementation;
        }
    }
}
