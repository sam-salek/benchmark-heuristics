/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.annotation;

import static org.junit.Assert.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

@SuppressWarnings({ "unchecked", "unused" })
public class MockInjectionUsingSetterOrPropertyTest extends TestBase {

    private SuperUnderTesting superUnderTestWithoutInjection = new SuperUnderTesting();

    @InjectMocks
    private SuperUnderTesting superUnderTest = new SuperUnderTesting();

    @InjectMocks
    private BaseUnderTesting baseUnderTest = new BaseUnderTesting();

    @InjectMocks
    private SubUnderTesting subUnderTest = new SubUnderTesting();

    @InjectMocks
    private OtherBaseUnderTesting otherBaseUnderTest = new OtherBaseUnderTesting();

    @InjectMocks
    private HasTwoFieldsWithSameType hasTwoFieldsWithSameType = new HasTwoFieldsWithSameType();

    private BaseUnderTesting baseUnderTestingInstance = new BaseUnderTesting();

    @InjectMocks
    private BaseUnderTesting initializedBase = baseUnderTestingInstance;

    @InjectMocks
    private BaseUnderTesting notInitializedBase;

    @Spy
    @InjectMocks
    private SuperUnderTesting initializedSpy = new SuperUnderTesting();

    @Spy
    @InjectMocks
    private SuperUnderTesting notInitializedSpy;

    @Mock
    private Map<?, ?> map;

    @Mock
    private List<?> list;

    @Mock
    private Set<?> histogram1;

    @Mock
    private Set<?> histogram2;

    @Mock
    private IMethods candidate2;

    @Spy
    private TreeSet<String> searchTree = new TreeSet<String>();

    private AutoCloseable session;

    @Before
    public void enforces_new_instances() {
        // openMocks called in TestBase Before method, so instances are not the same
        session = MockitoAnnotations.openMocks(this);
    }

    @After
    public void close_new_instances() throws Exception {
        if (session != null) {
            session.close();
        }
    }

    @Test
    public void should_keep_same_instance_if_field_initialized() {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

    @Test
    public void should_initialize_annotated_field_if_null() {
        assertNotNull(notInitializedBase);
    }

    @Test
    public void should_inject_mocks_in_spy() {
        assertNotNull(initializedSpy.getAList());
        assertTrue(MockUtil.isMock(initializedSpy));
    }

    @Test
    public void should_initialize_spy_if_null_and_inject_mocks() {
        assertNotNull(notInitializedSpy);
        assertNotNull(notInitializedSpy.getAList());
        assertTrue(MockUtil.isMock(notInitializedSpy));
    }

    @Test
    public void should_inject_mocks_if_annotated() {
        MockitoAnnotations.openMocks(this);
        assertSame(list, superUnderTest.getAList());
    }

    @Test
    public void should_not_inject_if_not_annotated() {
        MockitoAnnotations.openMocks(this);
        assertNull(superUnderTestWithoutInjection.getAList());
    }

    @Test
    public void should_inject_mocks_for_class_hierarchy_if_annotated() {
        MockitoAnnotations.openMocks(this);
        assertSame(list, baseUnderTest.getAList());
        assertSame(map, baseUnderTest.getAMap());
    }

    @Test
    public void should_inject_mocks_by_name() {
        MockitoAnnotations.openMocks(this);
        assertSame(histogram1, subUnderTest.getHistogram1());
        assertSame(histogram2, subUnderTest.getHistogram2());
    }

    @Test
    public void should_inject_spies() {
        MockitoAnnotations.openMocks(this);
        assertSame(searchTree, otherBaseUnderTest.getSearchTree());
    }

    @Test
    public void should_insert_into_field_with_matching_name_when_multiple_fields_of_same_type_exists_in_injectee() {
        MockitoAnnotations.openMocks(this);
        assertNull("not injected, no mock named 'candidate1'", hasTwoFieldsWithSameType.candidate1);
        assertNotNull("injected, there's a mock named 'candidate2'", hasTwoFieldsWithSameType.candidate2);
    }

    @Test
    public void should_instantiate_inject_mock_field_if_possible() throws Exception {
        assertNotNull(notInitializedBase);
    }

    @Test
    public void should_keep_instance_on_inject_mock_field_if_present() throws Exception {
        assertSame(baseUnderTestingInstance, initializedBase);
    }

    @Test
    public void should_report_nicely() throws Exception {
        Object failing = new Object() {

            @InjectMocks
            ThrowingConstructor failingConstructor;
        };
        try {
            MockitoAnnotations.openMocks(failing);
            fail();
        } catch (MockitoException e) {
            Assertions.assertThat(e.getMessage()).contains("failingConstructor").contains("constructor").contains("threw an exception");
            Assertions.assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

    static class ThrowingConstructor {

        ThrowingConstructor() {
            throw new RuntimeException("aha");
        }
    }

    static class SuperUnderTesting {

        private List<?> aList;

        public List<?> getAList() {
            return aList;
        }
    }

    static class BaseUnderTesting extends SuperUnderTesting {

        private Map<?, ?> aMap;

        public Map<?, ?> getAMap() {
            return aMap;
        }
    }

    static class OtherBaseUnderTesting extends SuperUnderTesting {

        private TreeSet<?> searchTree;

        public TreeSet<?> getSearchTree() {
            return searchTree;
        }
    }

    static class SubUnderTesting extends BaseUnderTesting {

        private Set<?> histogram1;

        private Set<?> histogram2;

        public Set<?> getHistogram1() {
            return histogram1;
        }

        public Set<?> getHistogram2() {
            return histogram2;
        }
    }

    static class HasTwoFieldsWithSameType {

        private IMethods candidate1;

        private IMethods candidate2;
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends org.mockitoutil.TestBase._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_keep_same_instance_if_field_initialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_keep_same_instance_if_field_initialized, this.description("should_keep_same_instance_if_field_initialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_initialize_annotated_field_if_null() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_initialize_annotated_field_if_null, this.description("should_initialize_annotated_field_if_null"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_inject_mocks_in_spy() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_inject_mocks_in_spy, this.description("should_inject_mocks_in_spy"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_initialize_spy_if_null_and_inject_mocks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_initialize_spy_if_null_and_inject_mocks, this.description("should_initialize_spy_if_null_and_inject_mocks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_inject_mocks_if_annotated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_inject_mocks_if_annotated, this.description("should_inject_mocks_if_annotated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_inject_if_not_annotated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_inject_if_not_annotated, this.description("should_not_inject_if_not_annotated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_inject_mocks_for_class_hierarchy_if_annotated() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_inject_mocks_for_class_hierarchy_if_annotated, this.description("should_inject_mocks_for_class_hierarchy_if_annotated"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_inject_mocks_by_name() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_inject_mocks_by_name, this.description("should_inject_mocks_by_name"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_inject_spies() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_inject_spies, this.description("should_inject_spies"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_insert_into_field_with_matching_name_when_multiple_fields_of_same_type_exists_in_injectee() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_insert_into_field_with_matching_name_when_multiple_fields_of_same_type_exists_in_injectee, this.description("should_insert_into_field_with_matching_name_when_multiple_fields_of_same_type_exists_in_injectee"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_instantiate_inject_mock_field_if_possible() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_instantiate_inject_mock_field_if_possible, this.description("should_instantiate_inject_mock_field_if_possible"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_keep_instance_on_inject_mock_field_if_present() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_keep_instance_on_inject_mock_field_if_present, this.description("should_keep_instance_on_inject_mock_field_if_present"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_report_nicely() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_report_nicely, this.description("should_report_nicely"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().enforces_new_instances();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().close_new_instances();
            super.after();
        }

        private MockInjectionUsingSetterOrPropertyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MockInjectionUsingSetterOrPropertyTest();
        }

        @java.lang.Override
        public MockInjectionUsingSetterOrPropertyTest implementation() {
            return this.implementation;
        }
    }
}
