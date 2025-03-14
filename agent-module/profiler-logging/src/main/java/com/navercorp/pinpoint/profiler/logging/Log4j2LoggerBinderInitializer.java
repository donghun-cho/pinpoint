/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.logging.PluginLoggerBinder;
import org.apache.logging.log4j.LogManager;

/**
 * For unit test to register/unregister loggerBinder.
 *
 * @author emeroad
 */
public class Log4j2LoggerBinderInitializer {

    private static final PluginLoggerBinder loggerBinder = new Log4j2Binder(LogManager.getContext());

    public static void beforeClass() {
        if (!PluginLogManager.initialize(loggerBinder)) {
            PluginLogger logger = loggerBinder.getLogger(Log4j2LoggerBinderInitializer.class.getName());
            logger.warn("LoggerBinder is already initialized");
        }
    }

    public static void afterClass() {
        PluginLogManager.unregister(loggerBinder);
    }
}
