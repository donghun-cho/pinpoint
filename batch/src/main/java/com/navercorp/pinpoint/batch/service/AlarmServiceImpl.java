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
package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.batch.dao.AlarmDao;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class AlarmServiceImpl implements AlarmService {

    private final AlarmDao alarmDao;

    public AlarmServiceImpl(AlarmDao alarmDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, CheckerResult> selectBeforeCheckerResults(String applicationName) {
        List<CheckerResult> checkerResultList = alarmDao.selectBeforeCheckerResultList(applicationName);
        if (checkerResultList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, CheckerResult> checkerResults = new HashMap<>();
        for (CheckerResult checkerResult : checkerResultList) {
            checkerResults.put(checkerResult.getRuleId(), checkerResult);
        }
        return checkerResults;
    }

    @Override
    public void updateBeforeCheckerResult(CheckerResult beforeCheckerResult, AlarmChecker<?> checker) {
        alarmDao.deleteCheckerResult(beforeCheckerResult.getRuleId());
        
        if (checker.isDetected()) {
            beforeCheckerResult.setDetected(true);
            beforeCheckerResult.increseCount();
            alarmDao.insertCheckerResult(beforeCheckerResult);
        } else {
            final Rule rule = checker.getRule();
            CheckerResult checkerResult = new CheckerResult(rule.getRuleId(), rule.getApplicationName(), rule.getCheckerName(), false, 0, 1);
            alarmDao.insertCheckerResult(checkerResult);
        }
        
         
    }
}
