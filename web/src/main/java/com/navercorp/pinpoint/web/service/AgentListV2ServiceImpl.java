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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AgentListV2ServiceImpl implements AgentListV2Service {
    private static final int UNDEFINED_SERVICE_TYPE_CODE = -1;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentProperties agentProperties;

    private final AgentIdDao agentIdDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    public AgentListV2ServiceImpl(AgentProperties agentProperties, AgentIdDao agentIdDao, MapAgentResponseDao mapAgentResponseDao) {
        this.agentProperties = Objects.requireNonNull(agentProperties, "agentProperties");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public List<AgentIdEntry> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        List<AgentIdEntry> agentIdEntryList = queryAllEntries(serviceUid.getUid(), applicationName, serviceType.getCode());
        return dedupeConsecutiveAgentId(agentIdEntryList);
    }

    @Override
    public List<AgentIdEntry> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        if (agentProperties.getStatisticsCheckServiceTypeCodes().contains((int) serviceType.getCode())) {
            return getAgentListByStatistics(serviceUid, applicationName, serviceType, range);
        }
        return getAgentListByStatus(serviceUid, applicationName, serviceType, range);
    }

    /**
     * For service types that may not send steady pings — fetch all entries, then filter by span statistics.
     */
    private List<AgentIdEntry> getAgentListByStatistics(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = queryAllEntries(serviceUid.getUid(), applicationName, serviceType.getCode());
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);
        // TODO use serviceUid to create Application
        Application searchApplication = new Application(applicationName, serviceType);
        agentIdEntryList = filterStatistics(agentIdEntryList, searchApplication, range);
        return agentIdEntryList;
    }

    /**
     * For service types that send pings — filter by status timestamp.
     */
    private List<AgentIdEntry> getAgentListByStatus(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = queryByMinStateTimestamp(serviceUid.getUid(), applicationName, serviceType.getCode(), range);
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);
        return agentIdEntryList;
    }

    private List<AgentIdEntry> queryAllEntries(int serviceUid, String applicationName, int serviceTypeCode) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        if (!agentProperties.getMissingHeaderServiceTypeCodes().contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE);
        return dedupeByKey(entries, undefinedEntries);
    }

    private List<AgentIdEntry> queryByMinStateTimestamp(int serviceUid, String applicationName, int serviceTypeCode, Range range) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntryByMinStateTimestamp(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        if (!agentProperties.getMissingHeaderServiceTypeCodes().contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntryByMinStateTimestamp(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE, range.getFrom());
        return dedupeByKey(entries, undefinedEntries);
    }

    private List<AgentIdEntry> filterByAgentStartTime(List<AgentIdEntry> entries, Range range) {
        return entries.stream()
                .filter(entry -> entry.getAgentStartTime() <= range.getTo())
                .toList();
    }

    private List<AgentIdEntry> filterStatistics(List<AgentIdEntry> agentIdEntries, Application searchApplication, Range range) {
        Set<String> statisticsAgentIds = mapAgentResponseDao.selectAgentIds(searchApplication, new TimeWindow(range));
        return agentIdEntries.stream()
                .filter(entry -> entry.getCurrentStateTimestamp() >= range.getFrom() || statisticsAgentIds.contains(entry.getAgentId()))
                .toList();
    }

    /**
     * Dedup by agentId + startTime, keeping the entry with the most recent state.
     */
    private static List<AgentIdEntry> dedupeByKey(List<AgentIdEntry> entries, List<AgentIdEntry> undefinedEntries) {
        Map<String, AgentIdEntry> deduped = new LinkedHashMap<>();
        for (AgentIdEntry entry : entries) {
            deduped.put(toDedupeKey(entry), entry);
        }
        for (AgentIdEntry entry : undefinedEntries) {
            deduped.merge(toDedupeKey(entry), entry, (existing, incoming) ->
                    incoming.getCurrentStateTimestamp() > existing.getCurrentStateTimestamp() ? incoming : existing);
        }
        return new ArrayList<>(deduped.values());
    }

    private static String toDedupeKey(AgentIdEntry entry) {
        return entry.getAgentId() + ":" + entry.getAgentStartTime();
    }

    private List<AgentIdEntry> dedupeConsecutiveAgentId(List<AgentIdEntry> orderedAgentIdEntryList) {
        List<AgentIdEntry> result = new ArrayList<>();
        AgentIdEntry previous = null;
        for (AgentIdEntry current : orderedAgentIdEntryList) {
            if (!isSameAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        logger.debug("dedupeConsecutiveAgentId input={}, result={}", orderedAgentIdEntryList.size(), result.size());
        return result;
    }

    private static boolean isSameAgentId(AgentIdEntry previous, AgentIdEntry current) {
        return previous != null &&
                previous.getAgentId().equals(current.getAgentId()) &&
                previous.getApplication().equals(current.getApplication());
    }
}