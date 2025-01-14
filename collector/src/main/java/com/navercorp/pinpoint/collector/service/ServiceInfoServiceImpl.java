package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ServiceIdDao;

import java.util.Objects;
import java.util.UUID;

public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceIdDao serviceIdDao;

    public ServiceInfoServiceImpl(ServiceIdDao serviceIdDao) {
        this.serviceIdDao = Objects.requireNonNull(serviceIdDao, "serviceIdDao");
    }

    @Override
    public UUID getServiceId(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");

        UUID serviceId = serviceIdDao.selectServiceId(serviceName);
        return serviceId;
    }

}
