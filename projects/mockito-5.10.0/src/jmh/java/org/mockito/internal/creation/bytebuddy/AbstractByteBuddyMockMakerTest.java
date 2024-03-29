/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.bytebuddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockitoutil.ClassLoaders.coverageTool;
import java.io.Serializable;
import net.bytebuddy.ByteBuddy;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.creation.AbstractMockMakerTest;
import org.mockito.internal.handler.MockHandlerImpl;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;
import org.mockito.plugins.MockMaker;
import org.mockitoutil.ClassLoaders;
import org.mockitoutil.SimpleSerializationUtil;
import org.objenesis.ObjenesisStd;

public abstract class AbstractByteBuddyMockMakerTest<MM extends MockMaker> extends AbstractMockMakerTest<MM, AbstractByteBuddyMockMakerTest.SomeClass> {

    protected AbstractByteBuddyMockMakerTest(MM mockMaker) {
        super(mockMaker, SomeClass.class);
    }

    protected abstract Class<?> mockTypeOf(Class<?> type);

    @Test
    public void should_create_mock_from_interface() throws Exception {
        SomeInterface proxy = mockMaker.createMock(settingsFor(SomeInterface.class), dummyHandler());
        Class<?> superClass = proxy.getClass().getSuperclass();
        assertThat(superClass).isEqualTo(Object.class);
    }

    @Test
    public void should_create_mock_from_class() throws Exception {
        ClassWithoutConstructor proxy = mockMaker.createMock(settingsFor(ClassWithoutConstructor.class), dummyHandler());
        Class<?> superClass = mockTypeOf(proxy.getClass());
        assertThat(superClass).isEqualTo(ClassWithoutConstructor.class);
    }

    @Test
    public void should_create_mock_from_class_even_when_constructor_is_dodgy() throws Exception {
        try {
            new ClassWithDodgyConstructor();
            fail();
        } catch (Exception expected) {
        }
        ClassWithDodgyConstructor mock = mockMaker.createMock(settingsFor(ClassWithDodgyConstructor.class), dummyHandler());
        assertThat(mock).isNotNull();
    }

    @Test
    public void should_use_ancillary_Types() {
        SomeClass mock = mockMaker.createMock(settingsFor(SomeClass.class, SomeInterface.class), dummyHandler());
        assertThat(mock).isInstanceOf(SomeInterface.class);
    }

    @Test
    public void should_create_class_by_constructor() {
        OtherClass mock = mockMaker.createMock(settingsWithConstructorFor(OtherClass.class), dummyHandler());
        assertThat(mock).isNotNull();
    }

    @Test
    public void should_allow_serialization() throws Exception {
        SerializableClass proxy = mockMaker.createMock(serializableSettingsFor(SerializableClass.class, SerializableMode.BASIC), dummyHandler());
        SerializableClass serialized = SimpleSerializationUtil.serializeAndBack(proxy);
        assertThat(serialized).isNotNull();
        MockHandler handlerOne = mockMaker.getHandler(proxy);
        MockHandler handlerTwo = mockMaker.getHandler(serialized);
        assertThat(handlerOne).isNotSameAs(handlerTwo);
    }

    @Test
    public void should_create_mock_from_class_with_super_call_to_final_method() throws Exception {
        MockCreationSettings<CallingSuperMethodClass> settings = settingsWithSuperCall(CallingSuperMethodClass.class);
        SampleClass proxy = mockMaker.createMock(settings, new MockHandlerImpl<CallingSuperMethodClass>(settings));
        assertThat(proxy.foo()).isEqualTo("foo");
    }

    class SomeClass {
    }

    interface SomeInterface {
    }

    static class OtherClass {
    }

    static class SerializableClass implements Serializable {
    }

    private class ClassWithoutConstructor {
    }

    private class ClassWithDodgyConstructor {

        public ClassWithDodgyConstructor() {
            throw new RuntimeException();
        }
    }

    @Test
    public void instantiate_fine_when_objenesis_on_the_classpath() throws Exception {
        // given
        ClassLoader classpath_with_objenesis = ClassLoaders.excludingClassLoader().withCodeSourceUrlOf(Mockito.class, ByteBuddy.class, ObjenesisStd.class).withCodeSourceUrlOf(coverageTool()).build();
        Class<?> mock_maker_class_loaded_fine_until = Class.forName("org.mockito.internal.creation.bytebuddy.SubclassByteBuddyMockMaker", true, classpath_with_objenesis);
        // when
        mock_maker_class_loaded_fine_until.getConstructor().newInstance();
        // then everything went fine
    }

    private static class SampleClass {

        public String foo() {
            return "foo";
        }
    }

    private static class CallingSuperMethodClass extends SampleClass {

        @Override
        public String foo() {
            return super.foo();
        }
    }
}
