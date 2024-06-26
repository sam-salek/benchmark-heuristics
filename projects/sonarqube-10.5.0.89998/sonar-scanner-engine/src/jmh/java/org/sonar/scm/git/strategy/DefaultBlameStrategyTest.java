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
package org.sonar.scm.git.strategy;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.event.Level;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTester;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.scm.git.strategy.DefaultBlameStrategy.BlameAlgorithmEnum.GIT_FILES_BLAME;
import static org.sonar.scm.git.strategy.DefaultBlameStrategy.BlameAlgorithmEnum.GIT_NATIVE_BLAME;

public class DefaultBlameStrategyTest {

    private final Configuration configuration = mock(Configuration.class);

    private final BlameStrategy underTest = new DefaultBlameStrategy(configuration);

    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void useRepositoryBlame_whenFileBlamePropsEnabled_shouldDisableRepoBlame() {
        when(configuration.get(DefaultBlameStrategy.PROP_SONAR_SCM_USE_BLAME_ALGORITHM)).thenReturn(Optional.of(GIT_FILES_BLAME.name()));
        assertThat(underTest.getBlameAlgorithm(1, 1)).isEqualTo(GIT_FILES_BLAME);
    }

    @Test
    public void useRepositoryBlame_whenFileBlamePropsDisableOrUnspecified_shouldEnableRepoBlame() {
        when(configuration.get(DefaultBlameStrategy.PROP_SONAR_SCM_USE_BLAME_ALGORITHM)).thenReturn(Optional.of(GIT_NATIVE_BLAME.name()));
        assertThat(underTest.getBlameAlgorithm(1, 10000)).isEqualTo(GIT_NATIVE_BLAME);
    }

    @Test
    public void useRepositoryBlame_whenFileBlamePropsInvalid_shouldThrowException() {
        when(configuration.get(DefaultBlameStrategy.PROP_SONAR_SCM_USE_BLAME_ALGORITHM)).thenReturn(Optional.of("unknown"));
        assertThatThrownBy(() -> underTest.getBlameAlgorithm(1, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void useRepositoryBlame_whenProcessorsCountAndFileSizeSpecified_shouldEnableRepoBlame() {
        when(configuration.getBoolean(DefaultBlameStrategy.PROP_SONAR_SCM_USE_BLAME_ALGORITHM)).thenReturn(Optional.empty());
        assertThat(underTest.getBlameAlgorithm(1, 10000)).isEqualTo(GIT_FILES_BLAME);
        assertThat(underTest.getBlameAlgorithm(8, 10)).isEqualTo(GIT_NATIVE_BLAME);
        assertThat(underTest.getBlameAlgorithm(1, 10)).isEqualTo(GIT_NATIVE_BLAME);
        assertThat(underTest.getBlameAlgorithm(1, 11)).isEqualTo(GIT_FILES_BLAME);
        assertThat(underTest.getBlameAlgorithm(0, 10)).isEqualTo(GIT_NATIVE_BLAME);
        assertThat(logTester.logs(Level.WARN)).contains("Available processors are 0. Falling back to native git blame");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_useRepositoryBlame_whenFileBlamePropsEnabled_shouldDisableRepoBlame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::useRepositoryBlame_whenFileBlamePropsEnabled_shouldDisableRepoBlame, this.description("useRepositoryBlame_whenFileBlamePropsEnabled_shouldDisableRepoBlame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_useRepositoryBlame_whenFileBlamePropsDisableOrUnspecified_shouldEnableRepoBlame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::useRepositoryBlame_whenFileBlamePropsDisableOrUnspecified_shouldEnableRepoBlame, this.description("useRepositoryBlame_whenFileBlamePropsDisableOrUnspecified_shouldEnableRepoBlame"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_useRepositoryBlame_whenFileBlamePropsInvalid_shouldThrowException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::useRepositoryBlame_whenFileBlamePropsInvalid_shouldThrowException, this.description("useRepositoryBlame_whenFileBlamePropsInvalid_shouldThrowException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_useRepositoryBlame_whenProcessorsCountAndFileSizeSpecified_shouldEnableRepoBlame() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::useRepositoryBlame_whenProcessorsCountAndFileSizeSpecified_shouldEnableRepoBlame, this.description("useRepositoryBlame_whenProcessorsCountAndFileSizeSpecified_shouldEnableRepoBlame"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().logTester, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private DefaultBlameStrategyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DefaultBlameStrategyTest();
        }

        @java.lang.Override
        public DefaultBlameStrategyTest implementation() {
            return this.implementation;
        }
    }
}
