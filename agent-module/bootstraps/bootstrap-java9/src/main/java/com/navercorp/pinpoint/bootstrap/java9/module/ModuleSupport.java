/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.java9.module;


import com.navercorp.pinpoint.bootstrap.module.JavaModule;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim - Add ServiceLoaderClassPathLookupHelper logic
 */
public class ModuleSupport {

    private final Instrumentation instrumentation;
    private final Consumer<String> logger;

    private final JavaModule javaBaseModule;
    private final JavaModule bootstrapModule;

    ModuleSupport(Instrumentation instrumentation, Consumer<String> logger) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.logger = Objects.requireNonNull(logger, "logger");

        this.javaBaseModule = wrapJavaModule(Object.class);
        this.bootstrapModule = wrapJavaModule(this.getClass());
    }

    @SuppressWarnings("unused") // Used implicitly
    public void setup() {
        // pinpoint module name : unnamed
        JavaModule bootstrapModule = getBootstrapModule();
        logger.accept("pinpoint Module id:" + bootstrapModule);
        logger.accept("pinpoint Module.isNamed:" + bootstrapModule.isNamed());
        logger.accept("pinpoint Module.name:" + bootstrapModule.getName());

        JavaModule baseModule = getJavaBaseModule();
        baseModule.addExports("jdk.internal.loader", bootstrapModule);
        baseModule.addExports("jdk.internal.misc", bootstrapModule);
        baseModule.addExports("jdk.internal.module", bootstrapModule);

//        baseModule.addExports("java.lang.reflect", bootstrapModule);

    }

    @SuppressWarnings("unused") // Used implicitly
    public void defineAgentModule(ClassLoader classLoader, URL[] jarFileList) {
        final JavaModule agentModule = newAgentModule(classLoader, jarFileList);

        prepareAgentModule(classLoader, agentModule);

//        addPermissionToLog4jModule(agentModule);
        addPermissionToLog4j2Module(agentModule);
        addPermissionToGuiceModule(agentModule);
        addPermissionToValueAnnotation(agentModule);
    }

    private void addPermissionToValueAnnotation(JavaModule agentModule) {
        JavaModule bootstrapModule = getBootstrapModule();

        agentModule.addOpens("com.navercorp.pinpoint.profiler.context.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.profiler.instrument.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.profiler.plugin.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.profiler.context.monitor.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.profiler.context.grpc.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.grpc.client.config", bootstrapModule);
        agentModule.addOpens("com.navercorp.pinpoint.profiler.micrometer.config", bootstrapModule);
    }

    private JavaModule newAgentModule(ClassLoader classLoader, URL[] jarFileList) {
        ModuleBuilder moduleBuilder = new ModuleBuilder(this.logger);
        final Module agentModule = moduleBuilder.defineModule(classLoader.getName(), classLoader, jarFileList);
        return wrapJavaModule(agentModule);
    }


    private void addPermissionToLog4jModule(JavaModule agentModule) {
        // required log4j
        // configuration parser
        JavaModule xmlModule = loadModule("java.xml");
        agentModule.addReads(xmlModule);
//      xml-api must be loaded from agentClassLoader -> ProfilerLibs.PINPOINT_PROFILER_CLASS
//        ClassLoader agentClassLoader = agentModule.getClass().getClassLoader();
//        Class.forName("javax.xml.parsers.DocumentBuilderFactory", false, agentClassLoader)
//        agentModule.addOpens("javax.xml.parsers.DocumentBuilderFactory");

        // PropertySetter bean.Introspector
        JavaModule desktopModule = loadModule("java.desktop");
        agentModule.addReads(desktopModule);
    }

    private void addPermissionToLog4j2Module(JavaModule agentModule) {
        // required org.apache.logging.log4j.util.Reflection
//        JavaModule reflect = loadModule("java.lang.reflect");
//        agentModule.addReads(reflect);
        // required log4j2
        // java.xml
        // pinpoint.agent/pinpoint.agent/org.apache.logging.log4j.core.config.xml.XmlConfiguration.<init>(XmlConfiguration.java:138)
        // java.desktop
        // pinpoint.agent/pinpoint.agent/org.apache.logging.log4j.core.LoggerContext.setConfiguration(LoggerContext.java:369)
        addPermissionToLog4jModule(agentModule);
    }

    private void addPermissionToGuiceModule(JavaModule agentModule) {
        JavaModule loggingModule = loadModule("java.logging");
        agentModule.addReads(loggingModule);

        // google guice
        // java.base does not "opens java.lang" to module pinpoint.agent
        // at pinpoint.agent/com.google.inject.internal.cglib.core.$ReflectUtils.<clinit>(ReflectUtils.java:42)
        JavaModule javaBaseModule = getJavaBaseModule();
        javaBaseModule.addOpens("java.lang", agentModule);
    }


    private void prepareAgentModule(final ClassLoader classLoader, JavaModule agentModule) {
        JavaModule bootstrapModule = getBootstrapModule();
        // Error:class com.navercorp.pinpoint.bootstrap.AgentBootLoader$1 cannot access class com.navercorp.pinpoint.profiler.DefaultAgent (in module pinpoint.agent)
        // because module pinpoint.agent does not export com.navercorp.pinpoint.profiler to unnamed module @7bfcd12c
        agentModule.addExports("com.navercorp.pinpoint.profiler", bootstrapModule);

        // Error:class com.navercorp.pinpoint.bootstrap.AgentBootLoader$1 cannot access class com.navercorp.pinpoint.profiler.test.PluginTestAgent (in module pinpoint.agent)
        // because module pinpoint.agent does not export com.navercorp.pinpoint.test to unnamed module @4b9e13df
        final String pinpointTestModule = "com.navercorp.pinpoint.profiler.test";
        if (agentModule.getPackages().contains(pinpointTestModule)) {
            agentModule.addExports(pinpointTestModule, bootstrapModule);
        } else {
            logger.accept(pinpointTestModule + " package not found");
        }

        agentModule.addReads(bootstrapModule);

        // Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make protected void java.net.URLClassLoader.addURL(java.net.URL) accessible:
        // module java.base does not "opens java.net" to module pinpoint.agent
        // at pinpoint.agent/pinpoint.agent/com.navercorp.pinpoint.profiler.instrument.classloading.URLClassLoaderHandler.<clinit>(URLClassLoaderHandler.java:44)
        JavaModule baseModule = getJavaBaseModule();
        baseModule.addOpens("java.net", agentModule);
        // java.lang.reflect.InaccessibleObjectException: Unable to make private java.nio.DirectByteBuffer(long,int) accessible: module java.base does not "opens java.nio" to module pinpoint.agent
        //   at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:337)
        baseModule.addOpens("java.nio", agentModule);

        // for Java9DefineClass
        baseModule.addExports("jdk.internal.misc", agentModule);
        if (ModuleUtils.jvmVersionUpper(11)) {
            final String internalAccessModule = "jdk.internal.access";
            if (baseModule.getPackages().contains(internalAccessModule)) {
                baseModule.addExports(internalAccessModule, agentModule);
            } else {
                logger.accept(internalAccessModule + " package not found");
            }
        }

        agentModule.addReads(baseModule);

        final JavaModule instrumentModule = loadModule("java.instrument");
        agentModule.addReads(instrumentModule);

        final JavaModule managementModule = loadModule("java.management");
        agentModule.addReads(managementModule);

        // DefaultCpuLoadMetric : com.sun.management.OperatingSystemMXBean
        final JavaModule jdkManagement = loadModule("jdk.management");
        agentModule.addReads(jdkManagement);

        // for grpc's NameResolverProvider
        final JavaModule jdkUnsupported = loadModule("jdk.unsupported");
        agentModule.addReads(jdkUnsupported);

//        LongAdder
//        final Module unsupportedModule = loadModule("jdk.unsupported");
//        Set<Module> readModules = Set.of(instrumentModule, managementModule, jdkManagement, unsupportedModule);

        // bootstrap ClassLoader --------------------------------
        ClassLoader bootstrapClassLoader = Object.class.getClassLoader();
        addUses("com.navercorp.pinpoint.common.trace.TraceMetadataProvider", bootstrapClassLoader, agentModule);

        addUses("com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin", bootstrapClassLoader, agentModule);

        // agent ClassLoader ------------------------------------------

        addUses("com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserProvider", classLoader, agentModule);

        addUses("io.grpc.NameResolverProvider", classLoader, agentModule);

        addUses("io.grpc.LoadBalancerProvider", classLoader, agentModule);

        addUses("org.apache.logging.log4j.spi.Provider", classLoader, agentModule);
        addUses("org.apache.logging.log4j.core.impl.Log4jProvider", classLoader, agentModule);
        addUses("org.apache.logging.log4j.core.util.ContextDataProvider", classLoader, agentModule);
        addUses("org.apache.logging.log4j.core.util.WatchEventService", classLoader, agentModule);
        addUses("org.apache.logging.log4j.util.PropertySource", classLoader, agentModule);

        addUses("org.slf4j.spi.SLF4JServiceProvider", classLoader, agentModule);

    }

    private void addUses(String className, ClassLoader classLoader, JavaModule agentModule) {
        Class<?> clazz = forName(className, classLoader);
        agentModule.addUses(clazz);
    }

    private Class<?> forName(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ModuleException(className + " not found Caused by:" + e.getMessage(), e);
        }
    }


    private JavaModule loadModule(String moduleName) {
        // force base-module loading
        logger.accept("loadModule:" + moduleName);
        final Module module = InternalModules.loadModule(moduleName);
        return wrapJavaModule(module);

//        final ModuleLayer boot = ModuleLayer.boot();
//        Optional<Module> optionalModule = boot.findModule(moduleName);
//        if (optionalModule.isPresent()) {
//            Module module = optionalModule.get();
//            return wrapJavaModule(module);
//        }
//        throw new ModuleException(moduleName + " not found");
    }

    private JavaModule wrapJavaModule(Class<?> clazz) {
        return new Java9Module(instrumentation, clazz.getModule());
    }

    private JavaModule wrapJavaModule(Module module) {
        return new Java9Module(instrumentation, module);
    }

    private JavaModule getJavaBaseModule() {
        return javaBaseModule;
    }

    private JavaModule getBootstrapModule() {
        return bootstrapModule;
    }

}
