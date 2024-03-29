package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.stubs.proxy.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.proxy.StubProxyStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashMap;
import java.util.Map;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class StubProxyConfigBuilderTest {

    private StubProxyConfig.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubProxyConfig.Builder();
    }

    @After
    public void cleanup() throws Exception {
        RegexParser.REGEX_PATTERN_CACHE.clear();
    }

    @Test
    public void stubbedProxyConfigDefaultStrategyNotAdditive() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.isAdditiveStrategy()).isFalse();
        assertThat(stubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.AS_IS);
    }

    @Test
    public void stubbedProxyConfigStrategyAdditive() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.withStrategy(StubProxyStrategy.ADDITIVE.toString()).build();
        assertThat(stubProxyConfig.isAdditiveStrategy()).isTrue();
        assertThat(stubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.ADDITIVE);
    }

    @Test
    public void stubbedProxyConfigHasNoHeaders() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.hasHeaders()).isFalse();
        assertThat(stubProxyConfig.getHeaders().isEmpty()).isTrue();
    }

    @Test
    public void stubbedProxyConfigHasDefaultUuid() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.build();
        assertThat(stubProxyConfig.getUUID()).isEqualTo("default");
    }

    @Test
    public void stubbedProxyConfigNameResetsToDefaultUuid() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.withUuid("newName").build();
        assertThat(stubProxyConfig.getUUID()).isEqualTo("newName");
        final StubProxyConfig freshStubProxyConfig = builder.build();
        assertThat(freshStubProxyConfig.getUUID()).isEqualTo("default");
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid(null).build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid(null).build();
        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig_WhenProxyConfigDescriptionDifferent() throws Exception {
        // proxy config description does NOT participate in equality
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("one").withDescription("description").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("one").build();
        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyNamesDifferent() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("one").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("two").build();
        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyPropertiesDifferent() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withProperty("key", "anotherValue").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withProperty("key", "value").build();
        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigReturnsExpectedEndpointAndDescription() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.withDescription("This is a proxy config for Google").withUuid("unique").withStrategy("as-is").withProperty("key", "value").withPropertyEndpoint("http://google.com").build();
        assertThat(stubProxyConfig.getPropertyEndpoint()).isEqualTo("http://google.com");
        assertThat(stubProxyConfig.getDescription()).isEqualTo("This is a proxy config for Google");
    }

    @Test
    public void stubbedProxyConfigEqualsAssertingConfig() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withHeader("headerKey", "headerValue").withProperty("key", "value").withPropertyEndpoint("http://google.com").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withHeader("headerKey", "headerValue").withProperty("key", "value").withPropertyEndpoint("http://google.com").build();
        assertThat(expectedStubProxyConfig.hasHeaders()).isTrue();
        assertThat(expectedStubProxyConfig.getHeaders().isEmpty()).isFalse();
        assertThat(assertingStubProxyConfig).isEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfigWithDifferentHeader() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withHeader("headerKey", "headerValue").withProperty("key", "value").withPropertyEndpoint("http://google.com").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withHeader("headerKey", "headerDifferentValue").withProperty("key", "value").withPropertyEndpoint("http://google.com").build();
        assertThat(expectedStubProxyConfig.hasHeaders()).isTrue();
        assertThat(expectedStubProxyConfig.getHeaders().isEmpty()).isFalse();
        assertThat(assertingStubProxyConfig.hasHeaders()).isTrue();
        assertThat(assertingStubProxyConfig.getHeaders().isEmpty()).isFalse();
        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigNotEqualsAssertingConfig() throws Exception {
        final StubProxyConfig expectedStubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withPropertyEndpoint("http://google.com").build();
        final StubProxyConfig assertingStubProxyConfig = builder.withUuid("unique").withStrategy("additive").withPropertyEndpoint("http://google.com").build();
        assertThat(assertingStubProxyConfig).isNotEqualTo(expectedStubProxyConfig);
    }

    @Test
    public void stubbedProxyConfigHashCode() throws Exception {
        final StubProxyConfig stubProxyConfigOne = builder.withUuid("unique").withStrategy("as-is").withPropertyEndpoint("http://google.com").build();
        final StubProxyConfig stubProxyConfigTwo = builder.withUuid("unique").withStrategy("as-is").withPropertyEndpoint("http://google.com").build();
        Map<StubProxyConfig, StubProxyConfig> mapping = new HashMap<>();
        mapping.put(stubProxyConfigOne, stubProxyConfigOne);
        mapping.put(stubProxyConfigTwo, stubProxyConfigTwo);
        assertThat(mapping.size()).isEqualTo(1);
    }

    @Test
    public void stubbedProxyConfigAsYaml() throws Exception {
        final StubProxyConfig stubProxyConfig = builder.withUuid("unique").withStrategy("as-is").withProperty("key", "value").withPropertyEndpoint("http://google.com").withProxyConfigAsYAML("- proxy-config:\n" + "    proxy-strategy: as-is\n" + "    proxy-properties:\n" + "      endpoint: https://jsonplaceholder.typicode.com").build();
        assertThat(stubProxyConfig.getProxyConfigAsYAML()).isEqualTo("- proxy-config:\n" + "    proxy-strategy: as-is\n" + "    proxy-properties:\n" + "      endpoint: https://jsonplaceholder.typicode.com");
    }

    @Test
    public void shouldThrowWhenUnexpectedProxyStrategyPassedIn() throws Exception {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.withUuid("unique").withStrategy("this-is-a-wrong-value").withPropertyEndpoint("http://google.com").build();
        });
        String expectedMessage = "this-is-a-wrong-value";
        String actualMessage = exception.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigDefaultStrategyNotAdditive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigDefaultStrategyNotAdditive, this.description("stubbedProxyConfigDefaultStrategyNotAdditive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigStrategyAdditive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigStrategyAdditive, this.description("stubbedProxyConfigStrategyAdditive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigHasNoHeaders() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigHasNoHeaders, this.description("stubbedProxyConfigHasNoHeaders"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigHasDefaultUuid() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigHasDefaultUuid, this.description("stubbedProxyConfigHasDefaultUuid"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigNameResetsToDefaultUuid() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigNameResetsToDefaultUuid, this.description("stubbedProxyConfigNameResetsToDefaultUuid"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull, this.description("stubbedProxyConfigEqualsAssertingConfig_WhenProxyNameNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigEqualsAssertingConfig_WhenProxyConfigDescriptionDifferent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigEqualsAssertingConfig_WhenProxyConfigDescriptionDifferent, this.description("stubbedProxyConfigEqualsAssertingConfig_WhenProxyConfigDescriptionDifferent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyNamesDifferent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyNamesDifferent, this.description("stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyNamesDifferent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyPropertiesDifferent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyPropertiesDifferent, this.description("stubbedProxyConfigNotEqualsAssertingConfig_WhenProxyPropertiesDifferent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigReturnsExpectedEndpointAndDescription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigReturnsExpectedEndpointAndDescription, this.description("stubbedProxyConfigReturnsExpectedEndpointAndDescription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigEqualsAssertingConfig() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigEqualsAssertingConfig, this.description("stubbedProxyConfigEqualsAssertingConfig"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigNotEqualsAssertingConfigWithDifferentHeader() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigNotEqualsAssertingConfigWithDifferentHeader, this.description("stubbedProxyConfigNotEqualsAssertingConfigWithDifferentHeader"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigNotEqualsAssertingConfig() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigNotEqualsAssertingConfig, this.description("stubbedProxyConfigNotEqualsAssertingConfig"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigHashCode() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigHashCode, this.description("stubbedProxyConfigHashCode"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stubbedProxyConfigAsYaml() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::stubbedProxyConfigAsYaml, this.description("stubbedProxyConfigAsYaml"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldThrowWhenUnexpectedProxyStrategyPassedIn() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldThrowWhenUnexpectedProxyStrategyPassedIn, this.description("shouldThrowWhenUnexpectedProxyStrategyPassedIn"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setUp();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().cleanup();
            super.after();
        }

        private StubProxyConfigBuilderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StubProxyConfigBuilderTest();
        }

        @java.lang.Override
        public StubProxyConfigBuilderTest implementation() {
            return this.implementation;
        }
    }
}
