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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.batch.config.AlarmCheckerConfiguration;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = AlarmCheckerConfiguration.class
)
public class CheckerCategoryTest {

    @Autowired
    CheckerRegistry registry;

    String applicationName = "appName";

    @Test
    public void configuration() {
        AlarmCheckerFactory checkerFactory = registry.getCheckerFactory(CheckerCategory.SLOW_COUNT);

        Rule rule = new Rule(applicationName, "", CheckerCategory.SLOW_COUNT.getName(), 75, "testGroup", false, false, false, "");
        SlowCountChecker checker = (SlowCountChecker) checkerFactory.createChecker(null, rule);
        rule = new Rule(applicationName, "", CheckerCategory.SLOW_COUNT.getName(), 63, "testGroup", false, false, false, "");
        SlowCountChecker checker2 = (SlowCountChecker) checkerFactory.createChecker(null, rule);

        assertNotSame(checker, checker2);

        assertNotNull(checker);
        assertEquals(75, (int) checker.getRule().getThreshold());

        assertNotNull(checker2);
        assertEquals(63, (int) checker2.getRule().getThreshold());
    }
}
