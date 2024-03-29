/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.withSettings;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockitoutil.TestBase;

@SuppressWarnings("unchecked")
public class MockUtilTest extends TestBase {

    @Test
    public void should_get_handler() {
        List<?> mock = Mockito.mock(List.class);
        assertNotNull(MockUtil.getMockHandler(mock));
    }

    @Test
    public void should_scream_when_not_a_mock_passed() {
        assertThatThrownBy(() -> {
            MockUtil.getMockHandler("");
        }).isInstanceOf(NotAMockException.class).hasMessage("Argument should be a mock, but is: class java.lang.String");
    }

    @Test
    public void should_scream_when_null_passed() {
        assertThatThrownBy(() -> {
            MockUtil.getMockHandler(null);
        }).isInstanceOf(NotAMockException.class).hasMessage("Argument should be a mock, but is null!");
    }

    @Test
    public void should_get_mock_settings() {
        List<?> mock = Mockito.mock(List.class);
        assertNotNull(MockUtil.getMockSettings(mock));
    }

    @Test
    public void should_validate_mock() {
        assertFalse(MockUtil.isMock("i mock a mock"));
        assertTrue(MockUtil.isMock(Mockito.mock(List.class)));
    }

    @Test
    public void should_validate_spy() {
        assertFalse(MockUtil.isSpy("i mock a mock"));
        assertFalse(MockUtil.isSpy(Mockito.mock(List.class)));
        assertFalse(MockUtil.isSpy(null));
        assertTrue(MockUtil.isSpy(Mockito.spy(new ArrayList())));
        assertTrue(MockUtil.isSpy(Mockito.spy(ArrayList.class)));
        assertTrue(MockUtil.isSpy(Mockito.mock(ArrayList.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))));
    }

    @Test
    public void should_redefine_MockName_if_default() {
        List<?> mock = Mockito.mock(List.class);
        MockUtil.maybeRedefineMockName(mock, "newName");
        Assertions.assertThat(MockUtil.getMockName(mock).toString()).isEqualTo("newName");
    }

    @Test
    public void should_not_redefine_MockName_if_default() {
        List<?> mock = Mockito.mock(List.class, "original");
        MockUtil.maybeRedefineMockName(mock, "newName");
        Assertions.assertThat(MockUtil.getMockName(mock).toString()).isEqualTo("original");
    }

    final class FinalClass {
    }

    class SomeClass {
    }

    interface SomeInterface {
    }

    @Test
    public void should_know_if_type_is_mockable() throws Exception {
        Assertions.assertThat(MockUtil.typeMockabilityOf(FinalClass.class, null).mockable()).isEqualTo(Plugins.getMockMaker().isTypeMockable(FinalClass.class).mockable());
        assertFalse(MockUtil.typeMockabilityOf(int.class, null).mockable());
        assertTrue(MockUtil.typeMockabilityOf(SomeClass.class, null).mockable());
        assertTrue(MockUtil.typeMockabilityOf(SomeInterface.class, null).mockable());
    }
}
