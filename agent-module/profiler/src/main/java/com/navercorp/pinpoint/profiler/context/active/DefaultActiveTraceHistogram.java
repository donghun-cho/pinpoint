/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;


import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultActiveTraceHistogram implements ActiveTraceHistogram {

    private final HistogramSchema histogramSchema;

    private int fastCount;
    private int normalCount;
    private int slowCount;
    private int verySlowCount;

    public DefaultActiveTraceHistogram(HistogramSchema histogramSchema) {
        this.histogramSchema = Objects.requireNonNull(histogramSchema, "histogramSchema");
    }

    public void increment(HistogramSlot slot) {
        Objects.requireNonNull(slot, "slot");

        final SlotType slotType = slot.getSlotType();
        switch (slotType) {
            case FAST:
                this.fastCount++;
                return;
            case NORMAL:
                this.normalCount++;
                return;
            case SLOW:
                this.slowCount++;
                return;
            case VERY_SLOW:
                this.verySlowCount++;
                return;
            default:
                throw new UnsupportedOperationException("slot type:" + slot);
        }
    }

    @Override
    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

    @Override
    public int getFastCount() {
        return fastCount;
    }

    @Override
    public int getNormalCount() {
        return normalCount;
    }

    @Override
    public int getSlowCount() {
        return slowCount;
    }

    @Override
    public int getVerySlowCount() {
        return verySlowCount;
    }

    @Override
    public List<Integer> getCounter() {
        return Arrays.asList(fastCount, normalCount, slowCount, verySlowCount);
    }

}
