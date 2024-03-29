/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import java.util.List;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.mock.SerializableMode;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

public class CreatingMocksWithConstructorTest extends TestBase {

    abstract static class AbstractMessage {

        private final String message;

        AbstractMessage() {
            this.message = "hey!";
        }

        AbstractMessage(String message) {
            this.message = message;
        }

        AbstractMessage(int i) {
            this.message = String.valueOf(i);
        }

        String getMessage() {
            return message;
        }
    }

    static class Message extends AbstractMessage {
    }

    class InnerClass extends AbstractMessage {
    }

    @Test
    public void can_create_mock_with_constructor() {
        Message mock = mock(Message.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        // the message is a part of state of the mocked type that gets initialized in constructor
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_mock_abstract_classes() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes() {
        AbstractMessage mock = spy(AbstractMessage.class);
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes_with_constructor_args() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor("hello!").defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hello!", mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes_with_constructor_primitive_args() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor(7).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("7", mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes_with_constructor_array_of_nulls() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor(new Object[] { null }).defaultAnswer(CALLS_REAL_METHODS));
        assertNull(mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes_with_casted_null() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor((String) null).defaultAnswer(CALLS_REAL_METHODS));
        assertNull(mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes_with_null_varargs() {
        try {
            mock(AbstractMessage.class, withSettings().useConstructor(null).defaultAnswer(CALLS_REAL_METHODS));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("constructorArgs should not be null. " + "If you need to pass null, please cast it to the right type, e.g.: useConstructor((String) null)");
        }
    }

    @Test
    public void can_mock_inner_classes() {
        InnerClass mock = mock(InnerClass.class, withSettings().useConstructor().outerInstance(this).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

    public static class ThrowingConstructorClass {

        public ThrowingConstructorClass() {
            throw new RuntimeException();
        }
    }

    @Test
    public void explains_constructor_exceptions() {
        try {
            mock(ThrowingConstructorClass.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
            fail();
        } catch (MockitoException e) {
            assertThat(e).hasRootCauseInstanceOf(RuntimeException.class);
            assertThat(e.getCause()).hasMessageContaining("Please ensure the target class has a 0-arg constructor and executes cleanly.");
        }
    }

    static class HasConstructor {

        HasConstructor(String x) {
        }
    }

    @Test
    public void exception_message_when_constructor_not_found() {
        try {
            // when
            spy(HasConstructor.class);
            // then
            fail();
        } catch (MockitoException e) {
            assertThat(e).hasMessage("Unable to create mock instance of type 'HasConstructor'");
            assertThat(e.getCause()).hasMessageContaining("Please ensure that the target class has a 0-arg constructor.");
        }
    }

    static class Base {
    }

    static class ExtendsBase extends Base {
    }

    static class ExtendsExtendsBase extends ExtendsBase {
    }

    static class UsesBase {

        public UsesBase(Base b) {
            constructorUsed = "Base";
        }

        public UsesBase(ExtendsBase b) {
            constructorUsed = "ExtendsBase";
        }

        private String constructorUsed = null;

        String getConstructorUsed() {
            return constructorUsed;
        }
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_base_class_exact_match() {
        UsesBase u = mock(UsesBase.class, withSettings().useConstructor(new Base()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("Base", u.getConstructorUsed());
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_extending_class_exact_match() {
        UsesBase u = mock(UsesBase.class, withSettings().useConstructor(new ExtendsBase()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("ExtendsBase", u.getConstructorUsed());
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_non_exact_match() {
        UsesBase u = mock(UsesBase.class, withSettings().useConstructor(new ExtendsExtendsBase()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("ExtendsBase", u.getConstructorUsed());
    }

    static class UsesTwoBases {

        public UsesTwoBases(Base b1, Base b2) {
            constructorUsed = "Base,Base";
        }

        public UsesTwoBases(ExtendsBase b1, Base b2) {
            constructorUsed = "ExtendsBase,Base";
        }

        public UsesTwoBases(Base b1, ExtendsBase b2) {
            constructorUsed = "Base,ExtendsBase";
        }

        private String constructorUsed = null;

        String getConstructorUsed() {
            return constructorUsed;
        }
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_multiple_base_class_exact_match() {
        UsesTwoBases u = mock(UsesTwoBases.class, withSettings().useConstructor(new Base(), new Base()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("Base,Base", u.getConstructorUsed());
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_first_extending_class_exact_match() {
        UsesTwoBases u = mock(UsesTwoBases.class, withSettings().useConstructor(new ExtendsBase(), new Base()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("ExtendsBase,Base", u.getConstructorUsed());
    }

    @Test
    public void can_mock_unambigous_constructor_with_inheritance_second_extending_class_exact_match() {
        UsesTwoBases u = mock(UsesTwoBases.class, withSettings().useConstructor(new Base(), new ExtendsBase()).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("Base,ExtendsBase", u.getConstructorUsed());
    }

    @Test
    public void fail_when_multiple_matching_constructors_with_inheritence() {
        try {
            // when
            mock(UsesTwoBases.class, withSettings().useConstructor(new ExtendsBase(), new ExtendsBase()));
            // then
            fail();
        } catch (MockitoException e) {
            // TODO the exception message includes Mockito internals like the name of the generated
            // class name.
            // I suspect that we could make this exception message nicer.
            assertThat(e).hasMessage("Unable to create mock instance of type 'UsesTwoBases'");
            assertThat(e.getCause()).hasMessageContaining("Multiple constructors could be matched to arguments of types " + "[org.mockitousage.constructor.CreatingMocksWithConstructorTest$ExtendsBase, " + "org.mockitousage.constructor.CreatingMocksWithConstructorTest$ExtendsBase]").hasMessageContaining("If you believe that Mockito could do a better job deciding on which constructor to use, please let us know.\n" + "Ticket 685 contains the discussion and a workaround for ambiguous constructors using inner class.\n" + "See https://github.com/mockito/mockito/issues/685");
        }
    }

    @Test
    public void mocking_inner_classes_with_wrong_outer_instance() {
        try {
            // when
            mock(InnerClass.class, withSettings().useConstructor().outerInstance(123).defaultAnswer(CALLS_REAL_METHODS));
            // then
            fail();
        } catch (MockitoException e) {
            assertThat(e).hasMessage("Unable to create mock instance of type 'InnerClass'");
            // TODO it would be nice if all useful information was in the top level exception,
            // instead of in the exception's cause
            // also applies to other scenarios in this test
            assertThat(e.getCause()).hasMessageContaining("Please ensure that the target class has a 0-arg constructor" + " and provided outer instance is correct.");
        }
    }

    @SuppressWarnings({ "CheckReturnValue", "MockitoUsage" })
    @Test
    public void mocking_interfaces_with_constructor() {
        // at the moment this is allowed however we can be more strict if needed
        // there is not much sense in creating a spy of an interface
        mock(IMethods.class, withSettings().useConstructor());
        spy(IMethods.class);
    }

    @Test
    public void prevents_across_jvm_serialization_with_constructor() {
        try {
            // when
            mock(AbstractMessage.class, withSettings().useConstructor().serializable(SerializableMode.ACROSS_CLASSLOADERS));
            // then
            fail();
        } catch (MockitoException e) {
            assertEquals("Mocks instantiated with constructor cannot be combined with " + SerializableMode.ACROSS_CLASSLOADERS + " serialization mode.", e.getMessage());
        }
    }

    abstract static class AbstractThing {

        abstract String name();

        String fullName() {
            return "abstract " + name();
        }
    }

    @Test
    public void abstract_method_returns_default() {
        AbstractThing thing = spy(AbstractThing.class);
        assertEquals("abstract null", thing.fullName());
    }

    @Test
    public void abstract_method_stubbed() {
        AbstractThing thing = spy(AbstractThing.class);
        when(thing.name()).thenReturn("me");
        assertEquals("abstract me", thing.fullName());
    }

    @Test
    public void interface_method_stubbed() {
        List<?> list = spy(List.class);
        when(list.size()).thenReturn(12);
        assertEquals(12, list.size());
    }

    @Test
    public void calls_real_interface_method() {
        List list = mock(List.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        assertNull(list.get(1));
    }

    @Test
    public void handles_bridge_methods_correctly() {
        SomeConcreteClass<Integer> testBug = spy(new SomeConcreteClass<Integer>());
        assertEquals("value", testBug.getValue(0));
    }

    @Test
    public void forbid_spy_on_mock() {
        try {
            spy(mock(List.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("Please don't pass mock here. Spy is not allowed on mock.");
        }
    }

    public abstract class SomeAbstractClass<T> {

        protected abstract String getRealValue(T value);

        public String getValue(T value) {
            return getRealValue(value);
        }
    }

    public class SomeConcreteClass<T extends Number> extends SomeAbstractClass<T> {

        @Override
        protected String getRealValue(T value) {
            return "value";
        }
    }

    private static class AmbiguousWithPrimitive {

        public AmbiguousWithPrimitive(String s, int i) {
            data = s;
        }

        public AmbiguousWithPrimitive(Object o, int i) {
            data = "just an object";
        }

        private String data;

        public String getData() {
            return data;
        }
    }

    @Test
    public void can_spy_ambiguius_constructor_with_primitive() {
        AmbiguousWithPrimitive mock = mock(AmbiguousWithPrimitive.class, withSettings().useConstructor("String", 7).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("String", mock.getData());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_create_mock_with_constructor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_create_mock_with_constructor, this.description("can_create_mock_with_constructor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_abstract_classes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_abstract_classes, this.description("can_mock_abstract_classes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes, this.description("can_spy_abstract_classes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes_with_constructor_args() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes_with_constructor_args, this.description("can_spy_abstract_classes_with_constructor_args"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes_with_constructor_primitive_args() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes_with_constructor_primitive_args, this.description("can_spy_abstract_classes_with_constructor_primitive_args"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes_with_constructor_array_of_nulls() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes_with_constructor_array_of_nulls, this.description("can_spy_abstract_classes_with_constructor_array_of_nulls"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes_with_casted_null() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes_with_casted_null, this.description("can_spy_abstract_classes_with_casted_null"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_abstract_classes_with_null_varargs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_abstract_classes_with_null_varargs, this.description("can_spy_abstract_classes_with_null_varargs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_inner_classes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_inner_classes, this.description("can_mock_inner_classes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_explains_constructor_exceptions() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::explains_constructor_exceptions, this.description("explains_constructor_exceptions"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exception_message_when_constructor_not_found() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::exception_message_when_constructor_not_found, this.description("exception_message_when_constructor_not_found"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_base_class_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_base_class_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_base_class_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_extending_class_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_extending_class_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_extending_class_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_non_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_non_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_non_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_multiple_base_class_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_multiple_base_class_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_multiple_base_class_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_first_extending_class_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_first_extending_class_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_first_extending_class_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_mock_unambigous_constructor_with_inheritance_second_extending_class_exact_match() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_mock_unambigous_constructor_with_inheritance_second_extending_class_exact_match, this.description("can_mock_unambigous_constructor_with_inheritance_second_extending_class_exact_match"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fail_when_multiple_matching_constructors_with_inheritence() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fail_when_multiple_matching_constructors_with_inheritence, this.description("fail_when_multiple_matching_constructors_with_inheritence"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mocking_inner_classes_with_wrong_outer_instance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mocking_inner_classes_with_wrong_outer_instance, this.description("mocking_inner_classes_with_wrong_outer_instance"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mocking_interfaces_with_constructor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mocking_interfaces_with_constructor, this.description("mocking_interfaces_with_constructor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prevents_across_jvm_serialization_with_constructor() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::prevents_across_jvm_serialization_with_constructor, this.description("prevents_across_jvm_serialization_with_constructor"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_abstract_method_returns_default() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::abstract_method_returns_default, this.description("abstract_method_returns_default"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_abstract_method_stubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::abstract_method_stubbed, this.description("abstract_method_stubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interface_method_stubbed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::interface_method_stubbed, this.description("interface_method_stubbed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_calls_real_interface_method() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::calls_real_interface_method, this.description("calls_real_interface_method"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_handles_bridge_methods_correctly() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::handles_bridge_methods_correctly, this.description("handles_bridge_methods_correctly"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_forbid_spy_on_mock() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::forbid_spy_on_mock, this.description("forbid_spy_on_mock"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_can_spy_ambiguius_constructor_with_primitive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::can_spy_ambiguius_constructor_with_primitive, this.description("can_spy_ambiguius_constructor_with_primitive"));
        }

        private CreatingMocksWithConstructorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CreatingMocksWithConstructorTest();
        }

        @java.lang.Override
        public CreatingMocksWithConstructorTest implementation() {
            return this.implementation;
        }
    }
}
