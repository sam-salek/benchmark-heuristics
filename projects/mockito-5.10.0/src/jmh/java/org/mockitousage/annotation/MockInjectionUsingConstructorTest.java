/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import java.util.AbstractCollection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockitousage.IMethods;
import org.mockitousage.examples.use.ArticleCalculator;
import org.mockitousage.examples.use.ArticleDatabase;
import org.mockitousage.examples.use.ArticleManager;

public class MockInjectionUsingConstructorTest {

    @Mock
    private ArticleCalculator calculator;

    @Mock
    private ArticleDatabase database;

    @InjectMocks
    private ArticleManager articleManager;

    @Spy
    @InjectMocks
    private ArticleManager spiedArticleManager;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldNotFailWhenNotInitialized() {
        assertNotNull(articleManager);
    }

    @Test
    public void innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior() {
        when(calculator.countArticles("new")).thenThrow(new IllegalArgumentException());
        assertThatThrownBy(() -> {
            articleManager.updateArticleCounters("new");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void mockJustWorks() {
        articleManager.updateArticleCounters("new");
    }

    @Test
    public void constructor_is_called_for_each_test_in_test_class() throws Exception {
        // given
        junit_test_with_3_tests_methods.constructor_instantiation = 0;
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new TextListener(System.out));
        // when
        jUnitCore.run(junit_test_with_3_tests_methods.class);
        // then
        assertThat(junit_test_with_3_tests_methods.constructor_instantiation).isEqualTo(3);
    }

    @Test
    public void objects_created_with_constructor_initialization_can_be_spied() throws Exception {
        assertFalse(MockUtil.isMock(articleManager));
        assertTrue(MockUtil.isMock(spiedArticleManager));
    }

    @Test
    public void should_report_failure_only_when_object_initialization_throws_exception() throws Exception {
        try {
            MockitoAnnotations.openMocks(new ATest());
            fail();
        } catch (MockitoException e) {
            assertThat(e.getMessage()).contains("failingConstructor").contains("constructor").contains("threw an exception");
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class junit_test_with_3_tests_methods {

        private static int constructor_instantiation = 0;

        @Mock
        List<?> some_collaborator;

        @InjectMocks
        some_class_with_parametered_constructor should_be_initialized_3_times;

        @Test
        public void test_1() {
        }

        @Test
        public void test_2() {
        }

        @Test
        public void test_3() {
        }

        private static class some_class_with_parametered_constructor {

            public some_class_with_parametered_constructor(List<?> collaborator) {
                constructor_instantiation++;
            }
        }
    }

    private static class FailingConstructor {

        FailingConstructor(Set<?> set) {
            throw new IllegalStateException("always fail");
        }
    }

    @Ignore("don't run this code in the test runner")
    private static class ATest {

        @Mock
        Set<?> set;

        @InjectMocks
        FailingConstructor failingConstructor;
    }

    @Test
    public void injectMocksMustFailWithInterface() {
        class TestCase {

            @InjectMocks
            IMethods f;
        }
        assertThatThrownBy(() -> {
            openMocks(new TestCase());
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Cannot instantiate @InjectMocks field named 'f'! Cause: the type 'IMethods' is an interface");
    }

    @Test
    public void injectMocksMustFailWithEnum() throws Exception {
        class TestCase {

            @InjectMocks
            TimeUnit f;
        }
        assertThatThrownBy(() -> {
            openMocks(new TestCase());
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Cannot instantiate @InjectMocks field named 'f'! Cause: the type 'TimeUnit' is an enum");
    }

    @Test
    public void injectMocksMustFailWithAbstractClass() {
        class TestCase {

            @InjectMocks
            AbstractCollection<?> f;
        }
        assertThatThrownBy(() -> {
            openMocks(new TestCase());
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Cannot instantiate @InjectMocks field named 'f'! Cause: the type 'AbstractCollection' is an abstract class");
    }

    @Test
    public void injectMocksMustFailWithNonStaticInnerClass() {
        class TestCase {

            class InnerClass {
            }

            @InjectMocks
            InnerClass f;
        }
        assertThatThrownBy(() -> {
            openMocks(new TestCase());
        }).isInstanceOf(MockitoException.class).hasMessageContaining("Cannot instantiate @InjectMocks field named 'f'! Cause: the type 'InnerClass' is an inner non static class");
    }

    static class StaticInnerClass {
    }

    @Test
    public void injectMocksMustSucceedWithStaticInnerClass() {
        class TestCase {

            @InjectMocks
            StaticInnerClass f;
        }
        TestCase testClass = new TestCase();
        openMocks(testClass);
        assertThat(testClass.f).isInstanceOf(StaticInnerClass.class);
    }

    @Test
    public void injectMocksMustSucceedWithInstance() {
        class TestCase {

            @InjectMocks
            StaticInnerClass f = new StaticInnerClass();
        }
        TestCase testClass = new TestCase();
        StaticInnerClass original = testClass.f;
        openMocks(testClass);
        assertThat(testClass.f).isSameAs(original);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotFailWhenNotInitialized() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotFailWhenNotInitialized, this.description("shouldNotFailWhenNotInitialized"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior, this.description("innerMockShouldRaiseAnExceptionThatChangesOuterMockBehavior"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mockJustWorks() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::mockJustWorks, this.description("mockJustWorks"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructor_is_called_for_each_test_in_test_class() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::constructor_is_called_for_each_test_in_test_class, this.description("constructor_is_called_for_each_test_in_test_class"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_objects_created_with_constructor_initialization_can_be_spied() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::objects_created_with_constructor_initialization_can_be_spied, this.description("objects_created_with_constructor_initialization_can_be_spied"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_report_failure_only_when_object_initialization_throws_exception() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_report_failure_only_when_object_initialization_throws_exception, this.description("should_report_failure_only_when_object_initialization_throws_exception"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustFailWithInterface() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustFailWithInterface, this.description("injectMocksMustFailWithInterface"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustFailWithEnum() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustFailWithEnum, this.description("injectMocksMustFailWithEnum"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustFailWithAbstractClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustFailWithAbstractClass, this.description("injectMocksMustFailWithAbstractClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustFailWithNonStaticInnerClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustFailWithNonStaticInnerClass, this.description("injectMocksMustFailWithNonStaticInnerClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustSucceedWithStaticInnerClass() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustSucceedWithStaticInnerClass, this.description("injectMocksMustSucceedWithStaticInnerClass"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_injectMocksMustSucceedWithInstance() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::injectMocksMustSucceedWithInstance, this.description("injectMocksMustSucceedWithInstance"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private MockInjectionUsingConstructorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new MockInjectionUsingConstructorTest();
        }

        @java.lang.Override
        public MockInjectionUsingConstructorTest implementation() {
            return this.implementation;
        }
    }
}
