/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlowCountCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    private static MapResponseDao mockMapResponseDAO;

    @BeforeAll
    public static void before() {
        mockMapResponseDAO = new MapResponseDao() {

            @Override
            public List<ResponseTime> selectResponseTime(Application application, TimeWindow timeWindow) {
                long timeStamp = 1409814914298L;
                ResponseTime.Builder responseTime = ResponseTime.newBuilder(SERVICE_NAME, ServiceType.STAND_ALONE, timeStamp);
                TimeHistogram histogram;
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        histogram = new TimeHistogram(ServiceType.STAND_ALONE, timeStamp);
                        histogram.addCallCountByElapsedTime(1000, false);
                        histogram.addCallCountByElapsedTime(3000, false);
                        histogram.addCallCountByElapsedTime(5000, false);
                        histogram.addCallCountByElapsedTime(6000, false);
                        histogram.addCallCountByElapsedTime(7000, false);
                        responseTime.addResponseTime("agent_" + i + "_" + j, histogram);
                    }

                    timeStamp += 1;
                }

                return List.of(responseTime.build());
            }
        };
    }

    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_COUNT.getName(), 74, "testGroup", false, false, false, "");
        SlowCountChecker checker = new SlowCountChecker(collector, rule);

        checker.check();
        assertTrue(checker.isDetected());
    }

    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_COUNT.getName(), 76, "testGroup", false, false, false, "");
        SlowCountChecker checker = new SlowCountChecker(collector, rule);

        checker.check();
        assertFalse(checker.isDetected());
    }
    
    /*
        the test data fetched directly from Hbase server.
        use this in case of need because the data is not kept usually.
    */
    /*
    @Autowired
    private HbaseMapResponseTimeDao hbaseMapResponseTimeDao;
    
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        SlowCountFilter filter = new SlowCountFilter(application);
        
        AlarmRuleResource rule = new AlarmRuleResource();
        rule.setThresholdRule(10);
        rule.setContinuousTime(36000000);
        filter.initialize(rule);
        
        DefaultAlarmEvent event = new DefaultAlarmEvent(System.currentTimeMillis(), hbaseMapResponseTimeDao);
        assertTrue(filter.check(event));
    }
    */
}
