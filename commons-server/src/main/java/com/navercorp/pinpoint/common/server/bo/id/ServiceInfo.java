package com.navercorp.pinpoint.common.server.bo.id;

import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServiceInfo {

    public static final UUID DEFAULT_SERVICE_ID = new UUID(0, 0);
    public static final ServiceInfo DEFAULT = new ServiceInfo(DEFAULT_SERVICE_ID, "DEFAULT", Collections.emptyMap());

    private final UUID serviceId;
    private final String serviceName;

    private final Map<String, String> tags;

    public ServiceInfo(UUID serviceId, String serviceName,
                       @Nullable Map<String, String> tags) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.tags = tags;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInfo that = (ServiceInfo) o;

        return serviceId.equals(that.serviceId);
    }

    @Override
    public int hashCode() {
        return serviceId.hashCode();
    }
}
