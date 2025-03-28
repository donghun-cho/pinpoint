/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.redis.lettuce;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class LettucePluginConfig {
    private final boolean enable;
    private final boolean tracePubSubListener;
    private final List<String> redisPubSubListenerBasePackageList;

    public LettucePluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.redis.lettuce.enable", true);
        this.tracePubSubListener = src.readBoolean("profiler.redis.lettuce.trace.pubsub-listener", true);
        this.redisPubSubListenerBasePackageList = src.readList("profiler.redis.lettuce.pubsub-listener.base-packages");
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isTracePubSubListener() {
        return tracePubSubListener;
    }

    public List<String> getRedisPubSubListenerBasePackageList() {
        return redisPubSubListenerBasePackageList;
    }

    @Override
    public String toString() {
        return "LettucePluginConfig{" +
                "enable=" + enable +
                ", tracePubSubListener=" + tracePubSubListener +
                ", redisPubSubListenerBasePackageList=" + redisPubSubListenerBasePackageList +
                '}';
    }
}