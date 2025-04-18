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

import com.navercorp.pinpoint.bootstrap.module.Providers;

import java.io.Closeable;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
class ModuleBuilder {

    private final Consumer<String> logger;

    public ModuleBuilder(Consumer<String> logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    Module defineModule(String moduleName, ClassLoader classLoader, URL[] urls) {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(urls, "urls");
        if (urls.length == 0) {
            throw new IllegalArgumentException("urls.length is 0");
        }
        logger.accept("bootstrap unnamedModule:" +  InternalModules.getUnnamedModule());
        logger.accept("platform unnamedModule:" + ClassLoader.getPlatformClassLoader().getUnnamedModule());
        logger.accept("system unnamedModule:" + ClassLoader.getSystemClassLoader().getUnnamedModule());

        Module unnamedModule = classLoader.getUnnamedModule();
        logger.accept("defineModule classLoader: " + classLoader);
        logger.accept("defineModule classLoader-unnamedModule: " + unnamedModule);


        List<PackageInfo> packageInfos = parsePackageInfo(urls);
        Set<String> packages = mergePackageInfo(packageInfos);
        logger.accept("packages:" + packages);
        Map<String, Set<String>> serviceInfoMap = mergeServiceInfo(packageInfos);
        logger.accept("providers:" + serviceInfoMap);

        ModuleDescriptor.Builder builder = ModuleDescriptor.newModule(moduleName);
        builder.packages(packages);
        for (Map.Entry<String, Set<String>> entry : serviceInfoMap.entrySet()) {
            builder.provides(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        ModuleDescriptor moduleDescriptor = builder.build();
        URI uri = getInformationURI(urls);

        ModuleLayer parent = ModuleLayer.boot();

        ModuleFinder before = new SingleModuleFinder(moduleDescriptor, uri);
        Configuration cf = parent.configuration().resolve(before, ModuleFinder.of(), Set.of(moduleName));

        ModuleLayer moduleLayer = ModuleLayer.defineModules(cf, List.of(parent), name -> classLoader).layer();
        Optional<Module> oModule = moduleLayer.findModule(moduleName);

        if (!oModule.isPresent()) {
            if (moduleLayer.modules().isEmpty()) {
                logger.accept("Attempt to create module " + moduleName + ", but nothing happened");
            } else {
                Module unknownModule = moduleLayer.modules().iterator().next();
                logger.accept("Attempt to create module " + moduleName + ", but ignored -> " + unknownModule.getName());
            }
            logger.accept("module name: " + moduleDescriptor.name());
            logger.accept("  - packages: " + moduleDescriptor.packages());
            logger.accept("  - providers: " + moduleDescriptor.provides());
            throw new IllegalStateException("Failed to create module-layer, module " + moduleName);
        }

        logger.accept("defineModule module:" + oModule.get());
        return oModule.get();
    }

    private Map<String, Set<String>> mergeServiceInfo(List<PackageInfo> packageInfos) {
        Map<String, Set<String>> providesMap = new HashMap<>();
        for (PackageInfo packageInfo : packageInfos) {
            List<Providers> serviceLoader = packageInfo.getProviders();
            for (Providers provides : serviceLoader) {
                Set<String> providerSet = providesMap.computeIfAbsent(provides.getService(), s -> new HashSet<>());
                providerSet.addAll(provides.getProviders());
            }
        }
        return providesMap;
    }

    private Set<String> mergePackageInfo(List<PackageInfo> packageInfos) {
        Set<String> packageSet = new HashSet<>();
        for (PackageInfo packageInfo : packageInfos) {
            packageSet.addAll(packageInfo.getPackage());
        }
        return packageSet;
    }

    private JarFile newJarFile(URL jarFile) {
        try {
            if (!jarFile.getProtocol().equals("file")) {
                throw new IllegalStateException("invalid file " + jarFile);
            }
            return new JarFile(jarFile.getFile());
        } catch (IOException e) {
            throw new ModuleException(jarFile.getFile() +  " create fail " + e.getMessage(), e);
        }
    }

    private URI getInformationURI(URL[] urls) {
        if (isEmpty(urls)) {
            return null;
        }
        final URL url = urls[0];
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isEmpty(URL[] urls) {
        return urls == null || urls.length == 0;
    }

    private List<PackageInfo> parsePackageInfo(URL[] urls) {

        final List<PackageInfo> packageInfoList = new ArrayList<>();
        for (URL url : urls) {
            if (!isJar(url)) {
                continue;
            }
            JarFile jarFile = null;
            try {
                jarFile = newJarFile(url);
                PackageAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
                PackageInfo packageInfo = packageAnalyzer.analyze();
                packageInfoList.add(packageInfo);
            } finally {
                close(jarFile);
            }
        }
        return packageInfoList;
    }

    private boolean isJar(URL url){
         // filter *.xml
        if (url.getPath().endsWith(".jar")) {
            return true;
        }
        return false;
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // skip
        }
    }

}
