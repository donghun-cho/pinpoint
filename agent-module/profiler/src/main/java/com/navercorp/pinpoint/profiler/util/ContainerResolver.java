/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;


import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
public class ContainerResolver {

    public static final String CONTAINER_PROPERTY_KEY = "pinpoint.container";
    public static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    public static final String DOT_DOCKER_ENV_FILEPATH = "/.dockerenv";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Properties properties;

    public ContainerResolver() {
        this(System.getProperties());
    }

    public ContainerResolver(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * Returns <tt>true</tt> if {@value #CONTAINER_PROPERTY_KEY} property exists and has no value or is <tt>true</tt>.
     *
     * @return <tt>true</tt> if {@value #CONTAINER_PROPERTY_KEY} property exists and has no value or is <tt>true</tt>.
     */
    public boolean isContainer() {
        if (properties.containsKey(CONTAINER_PROPERTY_KEY)) {
            return readPropertyBool(CONTAINER_PROPERTY_KEY);
        } else {
            return isDockerEnv();
        }
    }

    private boolean readPropertyBool(String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            logger.info("-D{} found", key);
            return true;
        }
        boolean boolValue = Boolean.parseBoolean(value);
        logger.info("-D{} found : {}, resolved to {}", key, value, boolValue);
        return boolValue;
    }

    private boolean isDockerEnv() {
        File file = new File(DOT_DOCKER_ENV_FILEPATH);
        return file.exists() || System.getenv(KUBERNETES_SERVICE_HOST) != null;
    }
}
