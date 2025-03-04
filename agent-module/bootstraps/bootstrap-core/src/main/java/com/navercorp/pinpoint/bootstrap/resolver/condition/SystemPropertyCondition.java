/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.resolver.condition;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Properties;

/**
 * @author HyunGil Jeong
 * 
 */
public class SystemPropertyCondition implements Condition<String>, ConditionValue<Properties> {

    public static final SystemPropertyCondition INSTANCE = new SystemPropertyCondition();

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass().getName());

    private final Properties property;
    
    private SystemPropertyCondition() {
        this(System.getProperties());
    }

    @VisibleForTesting
    SystemPropertyCondition(Properties property) {
        this.property = property;
    }
    
    /**
     * Checks if the specified value is in <tt>SimpleProperty</tt>.
     * 
     * @param requiredKey the values to check if they exist in <tt>SimpleProperty</tt>
     * @return <tt>true</tt> if the specified key is in <tt>SimpleProperty</tt>; 
     *         <tt>false</tt> if otherwise, or if <tt>null</tt> or empty key is provided
     */
    @Override
    public boolean check(String requiredKey) {
        if (StringUtils.isEmpty(requiredKey)) {
            return false;
        }
        if (this.property.getProperty(requiredKey) != null) {
            logger.debug("Property '{}' found in [{}]", requiredKey, this.property.getClass().getSimpleName());
            return true;
        } else {
            logger.debug("Property '{}' not found in [{}]", requiredKey, this.property.getClass().getSimpleName());
            return false;
        }
    }
    
    /**
     * Returns the <tt>SimpleProperty</tt>.
     * 
     * @return the {@link SimpleProperty} instance
     */
    @Override
    public Properties getValue() {
        return this.property;
    }
    
}
