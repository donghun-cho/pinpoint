package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServiceInfoService {
    List<String> selectAllServiceNames();

    String selectServiceName(UUID serviceId);

    UUID selectServiceId(String serviceName);

    ServiceInfo selectServiceInfo(String serviceName);

    void insertServiceInfo(String serviceName, @Nullable Map<String, String> tags);

    void updateServiceInfo(String serviceName, Map<String, String> tags);

    void deleteServiceInfo(String serviceName);
}
