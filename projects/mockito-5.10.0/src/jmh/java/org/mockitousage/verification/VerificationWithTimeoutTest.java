/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.junit.MockitoJUnit.rule;
import static org.mockitoutil.Stopwatch.createNotStarted;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.exceptions.verification.TooFewActualInvocations;
import org.mockito.internal.verification.DummyVerificationMode;
import org.mockito.junit.MockitoRule;
import org.mockito.verification.Timeout;
import org.mockito.verification.VerificationMode;
import org.mockitousage.IMethods;
import org.mockitoutil.Stopwatch;
import org.mockitoutil.async.AsyncTesting;

public class VerificationWithTimeoutTest {

    @Rule
    public MockitoRule mockito = rule();

    private Stopwatch watch = createNotStarted();

    @Mock
    private IMethods mock;

    private AsyncTesting async;

    @Before
    public void setUp() {
        async = new AsyncTesting();
    }

    @After
    public void tearDown() {
        async.cleanUp();
    }

    @Test
    public void should_verify_with_timeout() {
        // when
        async.runAfter(50, callMock('c'));
        async.runAfter(500, callMock('c'));
        // then
        verify(mock, timeout(200).only()).oneArg('c');
        // sanity check
        verify(mock).oneArg('c');
    }

    @Test
    public void should_verify_with_timeout_and_fail() {
        // when
        async.runAfter(200, callMock('c'));
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, timeout(50).only()).oneArg('c');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining("Wanted but not invoked");
        // TODO let's have a specific exception vs. generic assertion error + message
    }

    @Test
    // TODO nice to have
    @Ignore
    public void should_verify_with_timeout_and_fail_early() {
        // when
        callMock('c');
        callMock('c');
        watch.start();
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, timeout(2000)).oneArg('c');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining("Wanted but not invoked");
        watch.assertElapsedTimeIsLessThan(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void should_verify_with_times_x() {
        // when
        async.runAfter(50, callMock('c'));
        async.runAfter(100, callMock('c'));
        async.runAfter(600, callMock('c'));
        // then
        verify(mock, timeout(300).times(2)).oneArg('c');
    }

    @Test
    public void should_verify_with_times_x_and_fail() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(200, callMock('c'));
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, timeout(100).times(2)).oneArg('c');
            }
        }).isInstanceOf(TooFewActualInvocations.class);
    }

    @Test
    public void should_verify_with_at_least() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(50, callMock('c'));
        // then
        verify(mock, timeout(200).atLeast(2)).oneArg('c');
    }

    @Test
    public void should_verify_with_at_least_once() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(50, callMock('c'));
        // then
        verify(mock, timeout(200).atLeastOnce()).oneArg('c');
    }

    @Test
    public void should_verify_with_at_least_and_fail() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(50, callMock('c'));
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            public void call() {
                verify(mock, timeout(100).atLeast(3)).oneArg('c');
            }
        }).isInstanceOf(TooFewActualInvocations.class);
    }

    @Test
    public void should_verify_with_only() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(300, callMock('c'));
        // then
        verify(mock, timeout(100).only()).oneArg('c');
    }

    @Test
    public void should_return_formatted_output_from_toString_when_created_with_factory_method() {
        VerificationMode timeout = timeout(7);
        assertThat(timeout).hasToString("Wanted after at most 7 ms: [Wanted invocations count: 1]");
    }

    @Test
    public void should_return_formatted_output_from_toString_using_wrapped_verification_mode() {
        VerificationMode timeoutAndAtLeastOnce = new Timeout(9, new DummyVerificationMode());
        assertThat(timeoutAndAtLeastOnce).hasToString("Wanted after at most 9 ms: [Dummy verification mode]");
    }

    @Test
    public void should_return_formatted_output_from_toString_when_chaining_other_verification_mode() {
        VerificationMode timeoutAndOnly = timeout(7).only();
        assertThat(timeoutAndOnly).hasToString("Wanted after at most 7 ms: [Wanted invocations count: 1 and no other method invoked]");
    }

    @Test
    @Ignore("not testable, probably timeout().only() does not make sense")
    public void should_verify_with_only_and_fail() {
        // when
        async.runAfter(10, callMock('c'));
        async.runAfter(50, callMock('c'));
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, after(200).only()).oneArg('c');
            }
        }).isInstanceOf(AssertionError.class);
    }

    @Test
    // TODO nice to have
    @Ignore
    public void should_verify_with_only_and_fail_early() {
        // when
        callMock('c');
        callMock('c');
        watch.start();
        // then
        Assertions.assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() {
                verify(mock, timeout(2000).only()).oneArg('c');
            }
        }).isInstanceOf(AssertionError.class).hasMessageContaining(// TODO specific exception
        "Wanted but not invoked");
        watch.assertElapsedTimeIsLessThan(1000, TimeUnit.MILLISECONDS);
    }

    private Runnable callMock(final char c) {
        return new Runnable() {

            @Override
            public void run() {
                mock.oneArg(c);
            }
        };
    }
}
