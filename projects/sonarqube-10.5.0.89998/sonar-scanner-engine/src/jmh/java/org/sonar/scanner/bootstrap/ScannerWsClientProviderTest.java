/*
 * SonarQube
 * Copyright (C) 2009-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.scanner.bootstrap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.sonar.api.utils.System2;
import org.sonar.batch.bootstrapper.EnvironmentInformation;
import org.sonarqube.ws.client.HttpConnector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ScannerWsClientProviderTest {

    private ScannerWsClientProvider underTest = new ScannerWsClientProvider();

    private EnvironmentInformation env = new EnvironmentInformation("Maven Plugin", "2.3");

    @Test
    public void provide_client_with_default_settings() {
        ScannerProperties settings = new ScannerProperties(new HashMap<>());
        DefaultScannerWsClient client = underTest.provide(settings, env, new GlobalAnalysisMode(new ScannerProperties(Collections.emptyMap())), mock(System2.class), warning -> {
        });
        assertThat(client).isNotNull();
        assertThat(client.baseUrl()).isEqualTo("http://localhost:9000/");
        HttpConnector httpConnector = (HttpConnector) client.wsConnector();
        assertThat(httpConnector.baseUrl()).isEqualTo("http://localhost:9000/");
        assertThat(httpConnector.okHttpClient().proxy()).isNull();
        assertThat(httpConnector.okHttpClient().connectTimeoutMillis()).isEqualTo(5_000);
        assertThat(httpConnector.okHttpClient().readTimeoutMillis()).isEqualTo(60_000);
    }

    @Test
    public void provide_client_with_custom_settings() {
        Map<String, String> props = new HashMap<>();
        props.put("sonar.host.url", "https://here/sonarqube");
        props.put("sonar.token", "testToken");
        props.put("sonar.ws.timeout", "42");
        ScannerProperties settings = new ScannerProperties(props);
        DefaultScannerWsClient client = underTest.provide(settings, env, new GlobalAnalysisMode(new ScannerProperties(Collections.emptyMap())), mock(System2.class), warning -> {
        });
        assertThat(client).isNotNull();
        HttpConnector httpConnector = (HttpConnector) client.wsConnector();
        assertThat(httpConnector.baseUrl()).isEqualTo("https://here/sonarqube/");
        assertThat(httpConnector.okHttpClient().proxy()).isNull();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_provide_client_with_default_settings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::provide_client_with_default_settings, this.description("provide_client_with_default_settings"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_provide_client_with_custom_settings() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::provide_client_with_custom_settings, this.description("provide_client_with_custom_settings"));
        }

        private ScannerWsClientProviderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ScannerWsClientProviderTest();
        }

        @java.lang.Override
        public ScannerWsClientProviderTest implementation() {
            return this.implementation;
        }
    }
}
