/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.exceptions.verification.opentest4j.ArgumentsAreDifferent;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class BasicVerificationTest extends TestBase {

    @Mock
    private List<String> mock;

    @Mock
    private List<String> mockTwo;

    @Test
    public void shouldVerify() {
        mock.clear();
        verify(mock).clear();
        mock.add("test");
        verify(mock).add("test");
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void shouldFailVerification() {
        assertThatThrownBy(() -> {
            verify(mock).clear();
        }).isInstanceOf(WantedButNotInvoked.class).hasMessageContainingAll("Wanted but not invoked:", "mock.clear();", "-> at ", "Actually, there were zero interactions with this mock.");
    }

    @Test
    public void shouldFailVerificationOnMethodArgument() {
        mock.clear();
        mock.add("foo");
        verify(mock).clear();
        assertThatThrownBy(() -> {
            verify(mock).add("bar");
        }).isInstanceOf(ArgumentsAreDifferent.class);
    }

    @Test
    public void shouldFailOnWrongMethod() {
        mock.clear();
        mock.clear();
        mockTwo.add("add");
        verify(mock, atLeastOnce()).clear();
        verify(mockTwo, atLeastOnce()).add("add");
        try {
            verify(mockTwo, atLeastOnce()).add("foo");
            fail();
        } catch (WantedButNotInvoked e) {
        }
    }

    @Test
    public void shouldDetectRedundantInvocation() {
        mock.clear();
        mock.add("foo");
        mock.add("bar");
        verify(mock).clear();
        verify(mock).add("foo");
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
        }
    }

    @Test
    public void shouldDetectWhenInvokedMoreThanOnce() {
        mock.add("foo");
        mock.clear();
        mock.clear();
        verify(mock).add("foo");
        try {
            verify(mock).clear();
            fail();
        } catch (TooManyActualInvocations e) {
        }
    }

    @Test
    public void shouldVerifyStubbedMethods() {
        when(mock.add("test")).thenReturn(Boolean.FALSE);
        mock.add("test");
        verify(mock).add("test");
    }

    @Test
    public void shouldDetectWhenOverloadedMethodCalled() {
        IMethods mockThree = mock(IMethods.class);
        mockThree.varargs((Object[]) new Object[] {});
        try {
            verify(mockThree).varargs((String[]) new String[] {});
            fail();
        } catch (WantedButNotInvoked e) {
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerify() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerify, this.description("shouldVerify"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailVerification() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailVerification, this.description("shouldFailVerification"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailVerificationOnMethodArgument() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailVerificationOnMethodArgument, this.description("shouldFailVerificationOnMethodArgument"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailOnWrongMethod() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailOnWrongMethod, this.description("shouldFailOnWrongMethod"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetectRedundantInvocation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetectRedundantInvocation, this.description("shouldDetectRedundantInvocation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetectWhenInvokedMoreThanOnce() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetectWhenInvokedMoreThanOnce, this.description("shouldDetectWhenInvokedMoreThanOnce"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldVerifyStubbedMethods() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldVerifyStubbedMethods, this.description("shouldVerifyStubbedMethods"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDetectWhenOverloadedMethodCalled() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldDetectWhenOverloadedMethodCalled, this.description("shouldDetectWhenOverloadedMethodCalled"));
        }

        private BasicVerificationTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new BasicVerificationTest();
        }

        @java.lang.Override
        public BasicVerificationTest implementation() {
            return this.implementation;
        }
    }
}
