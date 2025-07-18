/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.encode;

import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ApplicationIndexRowKeyEncoder implements RowKeyEncoder<SpanBo> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationNameRowKeyEncoder rowKeyEncoder;
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private final RowKeyDistributor rowKeyDistributor;

    public ApplicationIndexRowKeyEncoder(ApplicationNameRowKeyEncoder rowKeyEncoder,
                                         RowKeyDistributor rowKeyDistributor) {
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    @Override
    public byte[] encodeRowKey(SpanBo span) {
        // distribute key evenly
        long acceptedTime = span.getCollectorAcceptTime();
        byte fuzzyKey = fuzzyRowKeyFactory.getKey(span.getElapsed());
        final byte[] appTraceIndexRowKey = newRowKey(span.getApplicationName(), acceptedTime, fuzzyKey);
        return rowKeyDistributor.getDistributedKey(appTraceIndexRowKey);
    }

    byte[] newRowKey(String applicationName, long acceptedTime, byte fuzzySlotKey) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("fuzzySlotKey:{}", fuzzySlotKey);
        }
        return rowKeyEncoder.encodeFuzzyRowKey(applicationName, acceptedTime, fuzzySlotKey);
    }
}
