/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.util.ClassLoaderUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ReflectionDependencyResolverTest {

    @Test
    public void get() throws Exception {
        ClassLoader contextClassLoader = ClassLoaderUtils.getContextClassLoader();
        ReflectionDependencyResolver dependencyResolver = new ReflectionDependencyResolver(contextClassLoader, new String[]{});
        List<Path> files = dependencyResolver.lookup(Collections.singletonList("commons-logging:commons-logging:1.2"));
        assertThat(files).hasSize(1);
    }
}