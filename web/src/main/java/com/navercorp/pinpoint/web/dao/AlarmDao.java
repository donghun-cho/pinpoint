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
package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.vo.UserGroup;

import java.util.List;

/**
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
public interface AlarmDao {

    String insertRule(Rule rule);
    
    String insertRuleExceptWebhookSend(Rule rule);

    void deleteRule(Rule rule);

    List<Rule> selectRuleByUserGroupId(String userGroupId);
    
    List<Rule> selectRuleByApplicationName(String applicationName);

    List<String> selectApplicationName();

    void updateRule(Rule rule);
    
    void updateRuleExceptWebhookSend(Rule rule);


    void deleteCheckerResult(String ruleId);

    void updateUserGroupIdOfRule(UserGroup userGroup);
}
