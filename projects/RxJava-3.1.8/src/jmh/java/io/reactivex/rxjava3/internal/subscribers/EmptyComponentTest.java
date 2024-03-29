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
package io.reactivex.rxjava3.internal.subscribers;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.EmptyComponent;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class EmptyComponentTest extends RxJavaTest {

    @Test
    public void normal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestHelper.checkEnum(EmptyComponent.class);
            EmptyComponent c = EmptyComponent.INSTANCE;
            assertTrue(c.isDisposed());
            c.request(10);
            c.request(-10);
            Disposable d = Disposable.empty();
            c.onSubscribe(d);
            assertTrue(d.isDisposed());
            BooleanSubscription s = new BooleanSubscription();
            c.onSubscribe(s);
            assertTrue(s.isCancelled());
            c.onNext(null);
            c.onNext(1);
            c.onComplete();
            c.onError(new TestException());
            c.onSuccess(2);
            c.cancel();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        private EmptyComponentTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new EmptyComponentTest();
        }

        @java.lang.Override
        public EmptyComponentTest implementation() {
            return this.implementation;
        }
    }
}
