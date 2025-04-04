/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.agent.PluginTestAgentStarter;
import com.navercorp.pinpoint.test.plugin.classloader.PluginAgentTestClassLoader;
import com.navercorp.pinpoint.test.plugin.classloader.PluginTestJunitTestClassLoader;
import com.navercorp.pinpoint.test.plugin.util.FileUtils;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;
import org.junit.platform.commons.JUnitException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PluginTestInstanceFactory {

    private final PluginTestContext context;

    public PluginTestInstanceFactory(PluginTestContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public PluginTestInstance create(ClassLoader parentClassLoader, String testId,
                                     PluginAgentTestClassLoader agentClassLoader,
                                     List<Path> libs,
                                     List<String> transformIncludeList,
                                     ClassLoding classLoading) throws ClassNotFoundException {
        final String id = testId + ":" + classLoading;
        PluginTestInstanceCallback instanceContext = startAgent(context.getConfigFile().toString(), agentClassLoader);

        List<Path> classPath = getClassPath(libs, this.context);
        final URL[] urls = URLUtils.pathToUrls(classPath);

        PluginTestJunitTestClassLoader testClassLoader = new PluginTestJunitTestClassLoader(urls, parentClassLoader, instanceContext);
        testClassLoader.setAgentClassLoader(agentClassLoader);
        testClassLoader.setTransformIncludeList(transformIncludeList);
        agentClassLoader.setTestClassLoader(testClassLoader);

        final Class<?> testClass = testClassLoader.loadClass(context.getTestClass().getName());
        return new DefaultPluginTestInstance(id, testClassLoader, testClass, context.isManageTraceObject(), instanceContext);
    }

    List<Path> getClassPath(List<Path> libs, PluginTestContext context) {
        final List<Path> libList = new ArrayList<>(16);
        libList.addAll(FileUtils.toPaths(context.getJunitLibList()));
        libList.addAll(libs);
        libList.add(context.getTestClassLocation());
        return libList;
    }

    PluginTestInstanceCallback startAgent(String configFile, ClassLoader classLoader) {
        try {
            Class<?> testClass = classLoader.loadClass(PluginTestAgentStarter.class.getName());
            Constructor<?> constructor = testClass.getConstructor(String.class);
            Method method = testClass.getDeclaredMethod("getCallback");
            return (PluginTestInstanceCallback) method.invoke(constructor.newInstance(configFile));
        } catch (Exception e) {
            throw new JUnitException("agent configFile=" + configFile + ", classLoader=" + classLoader, e);
        }
    }
}
