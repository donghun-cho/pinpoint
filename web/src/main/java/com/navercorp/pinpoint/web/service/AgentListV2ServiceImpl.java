package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.config.AgentListProperties;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AgentListV2ServiceImpl implements AgentListV2Service {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentIdDao agentIdDao;
    private final AgentLifeCycleDao agentLifeCycleDao;
    private final AgentListProperties agentListProperties;

    public AgentListV2ServiceImpl(AgentIdDao agentIdDao,
                                  AgentLifeCycleDao agentLifeCycleDao, AgentListProperties agentListProperties) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.agentListProperties = agentListProperties;
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName) {
        return getAgentItemList(serviceUid, applicationName, null, null);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        return getAgentItemList(serviceUid, applicationName, serviceType, null);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, Range range) {
        return getAgentItemList(serviceUid, applicationName, null, range);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        return getAgentItemList(serviceUid, applicationName, serviceType, range);
    }

    private List<AgentListItem> getAgentItemList(
            ServiceUid serviceUid,
            String applicationName,
            @Nullable ServiceType serviceType,
            @Nullable Range range) {
        List<AgentListItem> agentList = queryAgentList(serviceUid, applicationName, serviceType);

        // keep only the latest entry per agentId based on pre-ordered result.
        agentList = removeConsecutiveDuplicateAgentId(agentList);
        if (range != null) {
            agentList = filterTimestampByRange(agentList, range,
                    agentListProperties.getInactiveThresholdMillis(), agentListProperties.getInactiveThresholdExcludedServiceTypeCodes());
        }

        addStatus(agentList);
        if (range != null) {
            agentList = filterStatusByRange(agentList, range);
        }
        return agentList;
    }

    private List<AgentListItem> queryAgentList(
            ServiceUid serviceUid,
            String applicationName,
            @Nullable ServiceType serviceType) {
        if (serviceType == null) {
            return agentIdDao.getAgentListItems(serviceUid.getUid(), applicationName);
        } else {
            return agentIdDao.getAgentListItems(serviceUid.getUid(), applicationName, serviceType.getCode());
        }
    }

    private List<AgentListItem> removeConsecutiveDuplicateAgentId(List<AgentListItem> agentListItems) {
        List<AgentListItem> result = new ArrayList<>();
        String previousAgentId = null;
        for (AgentListItem item : agentListItems) {
            if (!item.getAgentId().equals(previousAgentId)) {
                result.add(item);
                previousAgentId = item.getAgentId();
            }
        }
        logger.debug("removeConsecutiveDuplicateAgentId input size: {}, result size: {}", agentListItems.size(), result.size());
        return result;
    }

    private List<AgentListItem> filterTimestampByRange(List<AgentListItem> agentListItems, Range range, long inactiveThresholdMillis, List<Integer> inactiveThresholdExcludedCodes) {
        return agentListItems.stream()
                .filter(item -> {
                    if (item.getStartTime() > range.getTo()) {
                        return false;
                    }
                    if (!inactiveThresholdExcludedCodes.contains(item.getApplication().getServiceTypeCode())) {
                        if (item.getLastUpdated() < range.getFrom() - inactiveThresholdMillis) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    // query agent status in parallel and apply
    private void addStatus(List<AgentListItem> agentListItems) {
        List<SimpleAgentKey> queryKeys = new ArrayList<>(agentListItems.size());
        for (AgentListItem agent : agentListItems) {
            SimpleAgentKey key = new SimpleAgentKey(agent.getAgentId(), agent.getStartTime());
            queryKeys.add(key);
        }
        List<Optional<AgentStatus>> agentStatusList = this.agentLifeCycleDao.getAgentStatus(queryKeys);

        for (int i = 0; i < agentListItems.size(); i++) {
            agentListItems.get(i).setAgentStatus(agentStatusList.get(i).orElse(AgentStatus.UNKNOWN));
        }
    }

    private List<AgentListItem> filterStatusByRange(List<AgentListItem> agentListItems, Range range) {
        return agentListItems.stream()
                .filter(agent -> {
                    AgentStatus agentStatus = agent.getAgentStatus();
                    if (agentStatus == null) {
                        return false;
                    }
                    return agentStatus.getEventTimestamp() >= range.getFrom();
                })
                .toList();
    }
}
