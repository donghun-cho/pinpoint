/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.view.tree;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;

public record AgentIdView(Application application, String agentId, long agentStartTime, String agentName,
                          AgentLifeCycleState status, LastStatus lastStatus) {

    public static AgentIdView of(AgentIdEntry agentIdEntry) {
        return of(agentIdEntry, null);
    }

    public static AgentIdView of(AgentIdEntry agentIdEntry, Long toTimestamp) {
        return of(agentIdEntry.getApplication(), agentIdEntry.getAgentId(), agentIdEntry.getAgentStartTime(), agentIdEntry.getAgentName(),
                agentIdEntry.getLastStatus(), agentIdEntry.getLastStatusTimestamp(), toTimestamp);
    }

    public static AgentIdView of(Application application, String agentId, long agentStartTime, String agentName,
                                 AgentLifeCycleState lastState, long lastStateTimestamp, Long toTimestamp) {
        final LastStatus lastStatus = new LastStatus(lastState, lastStateTimestamp);
        final AgentLifeCycleState effectiveStatus = calculateEffectiveStatus(lastStatus, toTimestamp);
        final String name = StringUtils.hasText(agentName) ? agentName : agentId;
        return new AgentIdView(application, agentId, agentStartTime, name,
                effectiveStatus, lastStatus);
    }

    public Application getApplication() {
        return application;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public AgentLifeCycleState getStatus() {
        return status;
    }

    public LastStatus getLastStatus() {
        return lastStatus;
    }

    private record LastStatus(AgentLifeCycleState state, long timestamp) {
    }

    private static AgentLifeCycleState calculateEffectiveStatus(LastStatus laststatus, Long toTimestamp) {
        if (toTimestamp != null && laststatus.timestamp() > toTimestamp) {
            return AgentLifeCycleState.RUNNING;
        }
        return laststatus.state();
    }
}
