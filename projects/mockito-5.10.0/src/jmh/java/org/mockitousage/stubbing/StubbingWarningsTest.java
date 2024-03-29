/*
 * Copyright (c) 2018 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.stubbing;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockitoutil.TestBase.filterLineNo;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.StateMaster;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.framework.DefaultMockitoSession;
import org.mockito.internal.util.SimpleMockitoLogger;
import org.mockito.quality.Strictness;
import org.mockitousage.IMethods;

public class StubbingWarningsTest {

    private static final String TEST_NAME = "test.name";

    @Mock
    IMethods mock;

    SimpleMockitoLogger logger = new SimpleMockitoLogger();

    MockitoSession mockito = new DefaultMockitoSession(singletonList((Object) this), TEST_NAME, Strictness.WARN, logger);

    @After
    public void after() {
        StateMaster stateMaster = new StateMaster();
        stateMaster.reset();
        stateMaster.clearMockitoListeners();
    }

    @Test
    public void few_interactions() {
        // when
        mock.simpleMethod(100);
        mock.otherMethod();
        // expect no exception
        mockito.finishMocking();
        logger.assertEmpty();
    }

    @Test
    public void stubbing_used() {
        // when
        given(mock.simpleMethod(100)).willReturn("100");
        mock.simpleMethod(100);
        // then
        mockito.finishMocking();
        logger.assertEmpty();
    }

    @Test
    public void unused_stubbed_is_not_implicitly_verified() {
        // when
        given(mock.simpleMethod(100)).willReturn("100");
        // <- stubbing is used
        mock.simpleMethod(100);
        // <- other method should not generate arg mismatch
        mock.simpleMethod(200);
        // then
        mockito.finishMocking();
        logger.assertEmpty();
    }

    @Test
    public void stubbing_argument_mismatch() {
        // when
        given(mock.simpleMethod(100)).willReturn("100");
        mock.simpleMethod(200);
        mockito.finishMocking();
        // TODO - currently we warn about "Unused" instead of "Arg mismatch" below
        // because it was simpler to implement. This can be improved given we put priority to
        // improve the warnings.
        // then
        assertEquals(filterLineNo("[MockitoHint] " + TEST_NAME + " (see javadoc for MockitoHint):\n" + "[MockitoHint] 1. Unused -> at org.mockitousage.stubbing.StubbingWarningsTest.stubbing_argument_mismatch(StubbingWarningsTest.java:0)\n"), filterLineNo(logger.getLoggedInfo()));
    }

    @Test
    public void unused_stubbing() {
        // when
        given(mock.simpleMethod(100)).willReturn("100");
        mockito.finishMocking();
        // then
        assertEquals(filterLineNo("[MockitoHint] " + TEST_NAME + " (see javadoc for MockitoHint):\n" + "[MockitoHint] 1. Unused -> at org.mockitousage.stubbing.StubbingWarningsTest.unused_stubbing(StubbingWarningsTest.java:0)\n"), filterLineNo(logger.getLoggedInfo()));
    }

    @SuppressWarnings({ "MockitoUsage", "CheckReturnValue" })
    @Test
    public void unfinished_verification_without_throwable() {
        // when
        verify(mock);
        assertThatThrownBy(() -> {
            mockito.finishMocking();
        }).isInstanceOf(MockitoException.class).hasMessageContainingAll("Missing method call for verify(mock) here:", "-> at", "Example of correct verification:", "    verify(mock).doSomething()", "Also, this error might show up because you verify either of: final/private/equals()/hashCode() methods.", "Those methods *cannot* be stubbed/verified.", "Mocking methods declared on non-public parent classes is not supported.");
    }

    @SuppressWarnings({ "MockitoUsage", "CheckReturnValue" })
    @Test
    public void unfinished_verification_with_throwable() {
        // when
        verify(mock);
        mockito.finishMocking(new AssertionError());
        // then
        logger.assertEmpty();
    }
}
