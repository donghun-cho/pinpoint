/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor.dao.hbase;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Taejin Koo
 */
public class BulkOperationReporter {

    private final LongAdder rejectedCount = new LongAdder();

    private final AtomicLong flushCount = new AtomicLong();

    private volatile long lastFlushTimeMillis;

    public void reportFlushAll() {
        flushCount.incrementAndGet();
        lastFlushTimeMillis = System.currentTimeMillis();
    }

    public void reportReject() {
        rejectedCount.increment();
    }

    public long getFlushAllCount() {
        return flushCount.get();
    }

    public long getRejectedCount() {
        return rejectedCount.sum();
    }

    public long getLastFlushTimeMillis() {
        return lastFlushTimeMillis;
    }

}
