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
package org.sonar.scanner.report;

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.scanner.cache.ScannerWriteCache;
import org.sonar.scanner.protocol.output.FileStructure;
import org.sonar.scanner.protocol.output.ScannerReportWriter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AnalysisCachePublisherTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private final ScannerWriteCache writeCache = mock(ScannerWriteCache.class);

    private final AnalysisCachePublisher publisher = new AnalysisCachePublisher(writeCache);

    private ScannerReportWriter scannerReportWriter;

    @Before
    public void before() throws IOException {
        FileStructure fileStructure = new FileStructure(temp.newFolder());
        scannerReportWriter = new ScannerReportWriter(fileStructure);
    }

    @Test
    public void publish_closes_cache() {
        publisher.publish(scannerReportWriter);
        verify(writeCache).close();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publish_closes_cache() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publish_closes_cache, this.description("publish_closes_cache"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().temp, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private AnalysisCachePublisherTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AnalysisCachePublisherTest();
        }

        @java.lang.Override
        public AnalysisCachePublisherTest implementation() {
            return this.implementation;
        }
    }
}
