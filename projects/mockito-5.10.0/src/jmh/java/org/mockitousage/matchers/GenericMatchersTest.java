/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.matchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockitoutil.TestBase;

public class GenericMatchersTest extends TestBase {

    private interface Foo {

        List<String> sort(List<String> otherList);

        String convertDate(Date date);
    }

    @Mock
    Foo sorter;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCompile() {
        when(sorter.convertDate(new Date())).thenReturn("one");
        when(sorter.convertDate((Date) any())).thenReturn("two");
        // following requires warning suppression but allows setting anyList()
        when(sorter.sort(ArgumentMatchers.<String>anyList())).thenReturn(null);
    }
}
