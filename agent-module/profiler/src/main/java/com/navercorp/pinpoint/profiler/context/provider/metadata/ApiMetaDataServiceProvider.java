/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApiMetaDataServiceProvider implements Provider<ApiMetaDataService> {

    private final Provider<DataSender<MetaDataType>> dataSenderProvider;
    private final SimpleCacheFactory simpleCacheFactory;

    @Inject
    public ApiMetaDataServiceProvider(@MetadataDataSender Provider<DataSender<MetaDataType>> dataSenderProvider, SimpleCacheFactory simpleCacheFactory) {
        this.dataSenderProvider = Objects.requireNonNull(dataSenderProvider, "dataSenderProvider");
        this.simpleCacheFactory = Objects.requireNonNull(simpleCacheFactory, "simpleCacheFactory");

    }

    @Override
    public ApiMetaDataService get() {
        final DataSender<MetaDataType> dataSender = this.dataSenderProvider.get();
        final SimpleCache<String> simpleCache = simpleCacheFactory.newSimpleCache();
        return new DefaultApiMetaDataService(dataSender, simpleCache);
    }
}
