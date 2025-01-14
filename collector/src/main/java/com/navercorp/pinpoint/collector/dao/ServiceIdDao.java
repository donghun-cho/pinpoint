package com.navercorp.pinpoint.collector.dao;

import java.util.UUID;

public interface ServiceIdDao {

    UUID selectServiceId(String serviceName);
}