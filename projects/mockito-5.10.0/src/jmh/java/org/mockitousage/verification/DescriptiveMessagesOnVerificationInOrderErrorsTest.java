/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class DescriptiveMessagesOnVerificationInOrderErrorsTest extends TestBase {

    private IMethods one;

    private IMethods two;

    private IMethods three;

    private InOrder inOrder;

    @Before
    public void setup() {
        one = Mockito.mock(IMethods.class);
        two = Mockito.mock(IMethods.class);
        three = Mockito.mock(IMethods.class);
        one.simpleMethod(1);
        one.simpleMethod(11);
        two.simpleMethod(2);
        two.simpleMethod(2);
        three.simpleMethod(3);
        inOrder = inOrder(one, two, three);
    }

    @Test
    public void shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(two, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(one, atLeastOnce()).simpleMethod(11);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expected = "\n" + "Verification in order failure" + "\n" + "Wanted but not invoked:" + "\n" + "iMethods.simpleMethod(11);" + "\n" + "-> at ";
            assertThat(e).hasMessageContaining(expected);
            String expectedCause = "\n" + "Wanted anywhere AFTER following interaction:" + "\n" + "iMethods.simpleMethod(2);" + "\n" + "-> at ";
            assertThat(e).hasMessageContaining(expectedCause);
        }
    }

    @Test
    public void shouldPrintVerificationInOrderErrorAndShowWantedOnly() {
        try {
            inOrder.verify(one).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String expected = "\n" + "Wanted but not invoked:" + "\n" + "iMethods.differentMethod();" + "\n" + "-> at";
            assertThat(e).hasMessageContaining(expected);
        }
    }

    @Test
    public void shouldPrintVerificationInOrderErrorAndShowWantedAndActual() {
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (org.mockito.exceptions.verification.opentest4j.ArgumentsAreDifferent e) {
            assertThat(e).hasMessageContaining("have different arguments");
        }
    }

    @Test
    public void shouldNotSayArgumentsAreDifferent() {
        // this is the last invocation so any next verification in order should simply say wanted
        // but not invoked
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertThat(e).hasMessageContaining("Wanted but not invoked");
        }
    }

    @Test
    public void shouldPrintMethodThatWasNotInvoked() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(three).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expectedMessage = "\n" + "Verification in order failure" + "\n" + "Wanted but not invoked:" + "\n" + "iMethods.simpleMethod(999);";
            assertThat(e).hasMessageContaining(expectedMessage);
        }
    }

    @Test
    public void shouldPrintTooManyInvocations() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        try {
            inOrder.verify(two, times(1)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expectedMessage = "\n" + "Verification in order failure:" + "\n" + "iMethods.simpleMethod(2);" + "\n" + "Wanted 1 time:" + "\n" + "-> at";
            assertThat(e).hasMessageContaining(expectedMessage);
            String expectedCause = "\n" + "But was 2 times:" + "\n" + "-> at";
            assertThat(e).hasMessageContaining(expectedCause);
        }
    }

    @Test
    public void shouldPrintTooFewInvocations() {
        two.simpleMethod(2);
        inOrder.verify(one, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three, atLeastOnce()).simpleMethod(3);
        try {
            inOrder.verify(two, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expectedMessage = "\n" + "Verification in order failure:" + "\n" + "iMethods.simpleMethod(2);" + "\n" + "Wanted 2 times:" + "\n" + "-> at";
            assertThat(e).hasMessageContaining(expectedMessage);
            String expectedCause = "\n" + "But was 1 time:" + "\n" + "-> at";
            assertThat(e).hasMessageContaining(expectedCause);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious, this.description("shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintVerificationInOrderErrorAndShowWantedOnly() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintVerificationInOrderErrorAndShowWantedOnly, this.description("shouldPrintVerificationInOrderErrorAndShowWantedOnly"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintVerificationInOrderErrorAndShowWantedAndActual() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintVerificationInOrderErrorAndShowWantedAndActual, this.description("shouldPrintVerificationInOrderErrorAndShowWantedAndActual"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotSayArgumentsAreDifferent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotSayArgumentsAreDifferent, this.description("shouldNotSayArgumentsAreDifferent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintMethodThatWasNotInvoked() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintMethodThatWasNotInvoked, this.description("shouldPrintMethodThatWasNotInvoked"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintTooManyInvocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintTooManyInvocations, this.description("shouldPrintTooManyInvocations"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldPrintTooFewInvocations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldPrintTooFewInvocations, this.description("shouldPrintTooFewInvocations"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setup();
        }

        private DescriptiveMessagesOnVerificationInOrderErrorsTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DescriptiveMessagesOnVerificationInOrderErrorsTest();
        }

        @java.lang.Override
        public DescriptiveMessagesOnVerificationInOrderErrorsTest implementation() {
            return this.implementation;
        }
    }
}
