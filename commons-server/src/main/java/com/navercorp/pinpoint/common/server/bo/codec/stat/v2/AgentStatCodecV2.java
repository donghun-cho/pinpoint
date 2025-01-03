/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class AgentStatCodecV2<T extends AgentStatDataPoint> implements AgentStatCodec<T> {

    private static final byte VERSION = 2;

    private final CodecFactory<T> codecFactory;

    public AgentStatCodecV2(final CodecFactory<T> codecFactory) {
        this.codecFactory = Objects.requireNonNull(codecFactory, "codecFactory");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<T> statDataPointList) {
        Assert.notEmpty(statDataPointList, "statDataPointList");

        final int numValues = statDataPointList.size();
        valueBuffer.putVInt(numValues);

        List<Long> startTimestamps = new ArrayList<>(numValues);
        List<Long> timestamps = new ArrayList<>(numValues);

        CodecEncoder<T> encoder = codecFactory.createCodecEncoder();
        for (T statDataPoint : statDataPointList) {
            startTimestamps.add(statDataPoint.getStartTimestamp());
            timestamps.add(statDataPoint.getTimestamp());
            encoder.addValue(statDataPoint);
        }

        final AgentStatDataPointCodec codec = codecFactory.getCodec();
        codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
        codec.encodeTimestamps(valueBuffer, timestamps);
        encoder.encode(valueBuffer);
    }

    @Override
    public List<T> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        final AgentStatDataPointCodec codec = codecFactory.getCodec();
        List<Long> startTimestamps = codec.decodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, numValues);
        List<Long> timestamps = codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        CodecDecoder<T> codecDecoder = codecFactory.createCodecDecoder();

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);

        codecDecoder.decode(valueBuffer, headerDecoder, numValues);

        List<T> result = new ArrayList<T>(numValues);
        for (int i = 0; i < numValues; i++) {
            T newObject = codecDecoder.getValue(i);
            newObject.setAgentId(agentId);
            newObject.setStartTimestamp(startTimestamps.get(i));
            newObject.setTimestamp(timestamps.get(i));
            result.add(newObject);
        }

        return result;
    }


}
