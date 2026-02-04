package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.mapper.Timestamped;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class ApplicationIndexV2CopyServiceImpl implements ApplicationIndexV2CopyService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentInfoDao agentInfoDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationIndexV2CopyServiceImpl(ApplicationIndexDao applicationIndexDao,
                                             AgentInfoDao agentInfoDao,
                                             ApplicationDao applicationDao,
                                             AgentIdDao agentIdDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public void copyApplication() {
        StopWatch stopWatch = new StopWatch("copyApplicationName");
        stopWatch.start("Select all applicationNames from v1");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();

        stopWatch.start("Insert all applicationNames to v2");
        for (Application application : applications) {
            applicationDao.insert(ServiceUid.DEFAULT.getUid(), application.getName(), application.getServiceTypeCode());
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId(int durationDays, int maxIteration, int batchSize) {
        StopWatch stopWatch = new StopWatch("copyAgentId");
        stopWatch.start("Copy agentId from v1 to v2 durationDays: " + durationDays);
        long fromTimestamp;
        if (durationDays <= 0) {
            // copy all
            fromTimestamp = 0;
        } else {
            fromTimestamp = System.currentTimeMillis() - Duration.ofDays(durationDays).toMillis();
        }

        String previousAgentId = null;
        long previousAgentStartTime = 0;
        int iteration = 0;
        while (iteration < maxIteration) {
            List<Timestamped<AgentInfoBo>> timestampedList = agentInfoDao.getAgentInfo(batchSize, fromTimestamp, previousAgentId, previousAgentStartTime);
            logger.info("iteration={}, fetched agentInfo size={}", iteration, timestampedList.size());
            if (timestampedList.isEmpty()) {
                break;
            }
            for (Timestamped<AgentInfoBo> timestamped : timestampedList) {
                agentIdDao.insert(timestamped.getValue(), timestamped.getTimestamp());
            }

            iteration++;
            AgentInfoBo lastAgentInfo = timestampedList.get(timestampedList.size() - 1).getValue();
            previousAgentId = lastAgentInfo.getAgentId();
            previousAgentStartTime = lastAgentInfo.getStartTime();
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());

        if (iteration >= 2_000_000) {
            logger.error("copyAgentId stopped by iteration limit: {}", iteration);
        }
    }
}
