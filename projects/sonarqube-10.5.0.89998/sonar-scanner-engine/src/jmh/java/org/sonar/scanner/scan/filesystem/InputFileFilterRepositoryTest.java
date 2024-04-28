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
package org.sonar.scanner.scan.filesystem;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class InputFileFilterRepositoryTest {

    @Test
    public void should_not_return_null_if_initialized_with_no_filters() {
        InputFileFilterRepository underTest = new InputFileFilterRepository();
        assertThat(underTest.getInputFileFilters()).isNotNull();
    }

    @Test
    public void should_return_filters_from_initialization() {
        InputFileFilterRepository underTest = new InputFileFilterRepository(f -> true);
        assertThat(underTest.getInputFileFilters()).isNotNull();
        assertThat(underTest.getInputFileFilters()).hasSize(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_not_return_null_if_initialized_with_no_filters() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_not_return_null_if_initialized_with_no_filters, this.description("should_not_return_null_if_initialized_with_no_filters"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_should_return_filters_from_initialization() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::should_return_filters_from_initialization, this.description("should_return_filters_from_initialization"));
        }

        private InputFileFilterRepositoryTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new InputFileFilterRepositoryTest();
        }

        @java.lang.Override
        public InputFileFilterRepositoryTest implementation() {
            return this.implementation;
        }
    }
}
