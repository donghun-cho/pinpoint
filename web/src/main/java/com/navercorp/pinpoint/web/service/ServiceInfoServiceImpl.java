package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.web.dao.ServiceIdDao;
import com.navercorp.pinpoint.web.dao.ServiceInfoDao;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ServiceInfoServiceImpl implements ServiceInfoService {
    private final ServiceIdDao serviceIdDao;
    private final ServiceInfoDao serviceInfoDao;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ServiceInfoServiceImpl(ServiceIdDao serviceIdDao, ServiceInfoDao serviceInfoDao) {
        this.serviceIdDao = Objects.requireNonNull(serviceIdDao, "serviceIdDao");
        this.serviceInfoDao = Objects.requireNonNull(serviceInfoDao, "serviceInfoDao");
    }

    @Override
    public List<String> selectAllServiceNames() {
        return serviceIdDao.selectAllServiceNames();
    }

    @Override
    public String selectServiceName(UUID serviceId) {
        if (serviceId == null) {
            return null;
        }
        return serviceInfoDao.selectServiceName(serviceId);
    }

    @Override
    public UUID selectServiceId(String serviceName) {
        return serviceIdDao.selectServiceId(serviceName);
    }

    @Override
    public ServiceInfo selectServiceInfo(String serviceName) {
        UUID serviceId = selectServiceId(serviceName);
        if (serviceId == null) {
            return null;
        }
        return serviceInfoDao.selectServiceInfo(serviceId);
    }

    @Override
    public void insertServiceInfo(String serviceName, @Nullable Map<String, String> tags) {
        // 1. insert (id -> name, info)
        UUID newServiceId = insertServiceInfoWithRetries(serviceName, tags, 3);
        if (newServiceId != null) {
            logger.info("saved (id:{} -> name:{})", newServiceId, serviceName);
        } else {
            throw new IllegalStateException("Failed to create new serviceId. serviceName: " + serviceName);
        }

        // 2. insert (name -> id)
        try {
            boolean insertResult = serviceIdDao.insertServiceIdIfNotExists(serviceName, newServiceId);
            if (insertResult) {
                logger.info("saved (name:{} -> id:{})", serviceName, newServiceId);
            } else {
                throw new IllegalStateException("already existing serviceName: " + serviceName);
            }
        } catch (Exception e) {
            serviceInfoDao.deleteServiceInfo(newServiceId);
            logger.error("failed to save (name:{} -> id:{})", serviceName, newServiceId, e);
            throw e;
        }
    }


    private UUID insertServiceInfoWithRetries(String serviceName, @Nullable Map<String, String> tags, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            UUID newServiceId = UUID.randomUUID();
            ServiceInfo serviceInfo = new ServiceInfo(newServiceId, serviceName, tags);

            boolean isSuccess = serviceInfoDao.insertServiceInfoIfNotExists(serviceInfo);
            if (isSuccess) {
                return newServiceId;
            }
        }
        logger.error("UUID collision occurred. serviceName: {}, maxRetries: {}", serviceName, maxRetries);
        return null;
    }


    @Override
    public void updateServiceInfo(String serviceName, Map<String, String> tags) {
        UUID serviceId = selectServiceId(serviceName);
        if (serviceId == null) {
            throw new IllegalArgumentException("serviceId not found. serviceName: " + serviceName);
        }

        serviceInfoDao.updateServiceInfo(serviceId, tags);
    }

    @Override
    public void deleteServiceInfo(String serviceName) {
        UUID serviceId = selectServiceId(serviceName);
        serviceIdDao.deleteServiceId(serviceName);
        if (serviceId != null) {
            serviceInfoDao.deleteServiceInfo(serviceId);
        }
    }
}
