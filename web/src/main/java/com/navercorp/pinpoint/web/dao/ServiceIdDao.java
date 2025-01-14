package com.navercorp.pinpoint.web.dao;

import java.util.List;
import java.util.UUID;

public interface ServiceIdDao {

    UUID selectServiceId(String serviceName);

    List<String> selectAllServiceNames();

    boolean insertServiceIdIfNotExists(String serviceName, UUID serviceId);

    void deleteServiceId(String serviceName);

}
