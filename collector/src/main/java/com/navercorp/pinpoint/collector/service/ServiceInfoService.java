package com.navercorp.pinpoint.collector.service;

import java.util.UUID;

public interface ServiceInfoService {
    UUID getServiceId(String serviceName);
}
