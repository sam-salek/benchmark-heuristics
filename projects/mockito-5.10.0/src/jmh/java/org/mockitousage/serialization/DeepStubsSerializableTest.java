/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.serialization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockitoutil.SimpleSerializationUtil.serializeAndBack;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class DeepStubsSerializableTest {

    @Test
    public void should_serialize_and_deserialize_mock_created_with_deep_stubs() throws Exception {
        // given
        SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(sampleClass.getSample().isFalse()).thenReturn(true);
        when(sampleClass.getSample().number()).thenReturn(999);
        // when
        SampleClass deserializedSample = serializeAndBack(sampleClass);
        // then
        assertThat(deserializedSample.getSample().isFalse()).isEqualTo(true);
        assertThat(deserializedSample.getSample().number()).isEqualTo(999);
    }

    @Test
    public void should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs() throws Exception {
        // given
        ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(deep_stubbed.iterator().next().add("yes")).thenReturn(true);
        // when
        ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);
        // then
        assertThat(deserialized_deep_stub.iterator().next().add("not stubbed but mock already previously resolved")).isEqualTo(false);
        assertThat(deserialized_deep_stub.iterator().next().add("yes")).isEqualTo(true);
    }

    @Test
    public void should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics() throws Exception {
        // given
        ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(deep_stubbed.iterator().hasNext()).thenReturn(true);
        ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);
        assertThat(deserialized_deep_stub.iterator().next()).isNull();
    }

    static class SampleClass implements Serializable {

        SampleClass2 getSample() {
            return new SampleClass2();
        }
    }

    static class SampleClass2 implements Serializable {

        boolean isFalse() {
            return false;
        }

        int number() {
            return 100;
        }
    }

    static class Container<E> implements Iterable<E>, Serializable {

        private E e;

        public Container(E e) {
            this.e = e;
        }

        public E get() {
            return e;
        }

        public Iterator<E> iterator() {
            return new Iterator<E>() {

                public boolean hasNext() {
                    return true;
                }

                public E next() {
                    return e;
                }

                public void remove() {
                }
            };
        }
    }

    static class ListContainer extends Container<List<String>> {

        public ListContainer(List<String> list) {
            super(list);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_serialize_and_deserialize_mock_created_with_deep_stubs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_serialize_and_deserialize_mock_created_with_deep_stubs, this.description("should_serialize_and_deserialize_mock_created_with_deep_stubs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs, this.description("should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics, this.description("should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics"));
        }

        private DeepStubsSerializableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DeepStubsSerializableTest();
        }

        @java.lang.Override
        public DeepStubsSerializableTest implementation() {
            return this.implementation;
        }
    }
}
