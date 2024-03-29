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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.operators.ScalarSupplier;

public class MaybeEmptyTest extends RxJavaTest {

    @Test
    public void scalarSupplier() {
        Maybe<Integer> m = Maybe.empty();
        assertTrue(m.getClass().toString(), m instanceof ScalarSupplier);
        assertNull(((ScalarSupplier<?>) m).get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarSupplier() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::scalarSupplier, this.description("scalarSupplier"));
        }

        private MaybeEmptyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MaybeEmptyTest();
        }

        @java.lang.Override
        public MaybeEmptyTest implementation() {
            return this.implementation;
        }
    }
}
