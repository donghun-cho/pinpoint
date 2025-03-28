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

package com.navercorp.pinpoint.collector.service.async;

import java.util.Map;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultAgentProperty implements AgentProperty {
    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final Map<?, ?> properties;
    private final int serviceType;

    public DefaultAgentProperty(String applicationName, int serviceType, String agentId, long agentStartTime, Map<?, ?> properties) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public int getServiceType() {
        return serviceType;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getStartTime() {
        return agentStartTime;
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return "DefaultAgentProperty{" +
                "applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", properties=" + properties +
                ", serviceType=" + serviceType +
                '}';
    }
}
