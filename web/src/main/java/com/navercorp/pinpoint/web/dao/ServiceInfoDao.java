package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;

import java.util.Map;
import java.util.UUID;

public interface ServiceInfoDao {

    String selectServiceName(UUID serviceId);

    ServiceInfo selectServiceInfo(UUID serviceId);

    boolean insertServiceInfoIfNotExists(ServiceInfo serviceInfo);

    void updateServiceInfo(UUID serviceId, Map<String, String> tags);

    void deleteServiceInfo(UUID serviceId);
}
