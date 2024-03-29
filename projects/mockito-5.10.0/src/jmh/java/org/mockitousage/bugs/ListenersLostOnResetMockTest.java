/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.bugs;

import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.Test;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

public class ListenersLostOnResetMockTest {

    @Test
    public void listener() throws Exception {
        InvocationListener invocationListener = mock(InvocationListener.class);
        List mockedList = mock(List.class, withSettings().invocationListeners(invocationListener));
        reset(mockedList);
        mockedList.clear();
        verify(invocationListener).reportInvocation(any(MethodInvocationReport.class));
    }
}
