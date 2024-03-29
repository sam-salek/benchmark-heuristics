/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.bugs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockitoutil.TestBase;

// see issue 101
public class CovariantOverrideTest extends TestBase {

    public interface ReturnsObject {

        Object callMe();
    }

    public interface ReturnsString extends ReturnsObject {

        // Java 5 covariant override of method from parent interface
        String callMe();
    }

    @Test
    public void returnFoo1() {
        ReturnsObject mock = mock(ReturnsObject.class);
        when(mock.callMe()).thenReturn("foo");
        // Passes
        assertEquals("foo", mock.callMe());
    }

    @Test
    public void returnFoo2() {
        ReturnsString mock = mock(ReturnsString.class);
        when(mock.callMe()).thenReturn("foo");
        // Passes
        assertEquals("foo", mock.callMe());
    }

    @Test
    public void returnFoo3() {
        ReturnsObject mock = mock(ReturnsString.class);
        when(mock.callMe()).thenReturn("foo");
        // Passes
        assertEquals("foo", mock.callMe());
    }

    @Test
    public void returnFoo4() {
        ReturnsString mock = mock(ReturnsString.class);
        // covariant override not generated
        mock.callMe();
        // Switch to base type to call covariant override
        ReturnsObject mock2 = mock;
        // Fails: java.lang.AssertionError: expected:<foo> but was:<null>
        verify(mock2).callMe();
    }
}
