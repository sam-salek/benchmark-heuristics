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

public class MaybeToObservableTest extends RxJavaTest {

    @Test
    public void source() {
        Maybe<Integer> m = Maybe.just(1);
        assertSame(m, (((HasUpstreamMaybeSource<?>) m.toObservable()).source()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToObservable(new Function<Maybe<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.toObservable();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private MaybeToObservableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeToObservableTest();
        }

        @java.lang.Override
        public MaybeToObservableTest implementation() {
            return this.implementation;
        }
    }
}
