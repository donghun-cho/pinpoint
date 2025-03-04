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

package com.navercorp.pinpoint.profiler.test;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContextModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.test.rpc.MockRpcModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class OverrideModuleFactory implements ModuleFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Module[] overrideModule;

    public OverrideModuleFactory(Module... overrideModule) {
        this.overrideModule = Objects.requireNonNull(overrideModule, "overrideModule");
    }

    @Override
    public Module newModule(AgentContextOption agentOption) {

        ModuleFactory moduleFactory = new ApplicationContextModuleFactory() {
            @Override
            protected Module newRpcModule(AgentContextOption agentOption) {
                logger.info("load {}", MockRpcModule.class.getName());
                return new MockRpcModule();
            }
        };
        Module module = moduleFactory.newModule(agentOption);
        return Modules.override(module).with(overrideModule);
    }
}
