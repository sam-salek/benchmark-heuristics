/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitointegration;

import static org.mockitoutil.ClassLoaders.coverageTool;
import java.util.Set;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.hamcrest.Matcher;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockitoutil.ClassLoaders;
import org.objenesis.Objenesis;

public class NoJUnitDependenciesTest {

    @Test
    public void pure_mockito_should_not_depend_JUnit___ByteBuddy() throws Exception {
        Assume.assumeTrue("ByteBuddyMockMaker".equals(Plugins.getMockMaker().getClass().getSimpleName()));
        ClassLoader classLoader_without_JUnit = ClassLoaders.excludingClassLoader().withCodeSourceUrlOf(Mockito.class, Matcher.class, ByteBuddy.class, ByteBuddyAgent.class, Objenesis.class).withCodeSourceUrlOf(coverageTool()).without("junit", "org.junit", "org.opentest4j").build();
        Set<String> pureMockitoAPIClasses = ClassLoaders.in(classLoader_without_JUnit).omit("runners", "junit", "JUnit", "opentest4j").listOwnedClasses();
        ClassLoadabilityChecker checker = new ClassLoadabilityChecker(classLoader_without_JUnit, "JUnit");
        // The later class is required to be initialized before any inline mock maker classes can be
        // loaded.
        checker.checkLoadability("org.mockito.internal.creation.bytebuddy.InlineDelegateByteBuddyMockMaker");
        pureMockitoAPIClasses.remove("org.mockito.internal.creation.bytebuddy.InlineDelegateByteBuddyMockMaker");
        for (String pureMockitoAPIClass : pureMockitoAPIClasses) {
            checker.checkLoadability(pureMockitoAPIClass);
        }
    }
}
