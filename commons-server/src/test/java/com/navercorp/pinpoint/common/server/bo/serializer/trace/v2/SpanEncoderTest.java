package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.thrift.SpanFactory;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEncoderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int REPEAT_COUNT = 10;

    private final long spanAcceptedTime = System.currentTimeMillis();

    private final RandomTSpan randomTSpan = new RandomTSpan();
    private final Random random = new Random();

    private final SpanFactory spanFactory = new SpanFactory();

    private final SpanEncoder spanEncoder = new SpanEncoderV0();
    private final SpanDecoder spanDecoder = new SpanDecoderV0();

    private final SpanEventFilter filter = new EmptySpanEventFilter();

    @BeforeEach
    public  void before() {
        JvmVersion version = JvmUtils.getVersion();
        Assumptions.assumeFalse(version.onOrAfter(JvmVersion.JAVA_17), "Skip test for Java 17+");
    }

    @Test
    public void testEncodeSpanColumnValue_simpleSpan() {
        SpanBo spanBo = randomSpan();
        assertSpan(spanBo);
    }


    @Test
    public void testEncodeSpanColumnValue_simpleSpan_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_simpleSpan();
        }
    }


    @Test
    public void testEncodeSpanColumnValue_complexSpan() {

        SpanBo spanBo = randomComplexSpan();
        assertSpan(spanBo);

    }

    @Test
    public void testEncodeSpanColumnValue_complexSpan_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_complexSpan();
        }
    }

    @Test
    public void testEncodeSpanColumnValue_simpleSpanChunk() {

        SpanChunkBo spanChunkBo = randomSpanChunk();
        assertSpanChunk(spanChunkBo);

    }

    @Test
    public void testEncodeSpanColumnValue_simpleSpanChunk_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_simpleSpanChunk();
        }
    }


    @Test
    public void testEncodeSpanColumnValue_complexSpanChunk() {

        SpanChunkBo spanChunkBo = randomComplexSpanChunk();
        assertSpanChunk(spanChunkBo);

    }

    @Test
    public void testEncodeSpanColumnValue_complexSpanChunk_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_complexSpanChunk();
        }
    }

    private long getCollectorAcceptTime() {
        long currentTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        long randomSeed = random.nextLong(0, TimeUnit.DAYS.toMillis(60));
        return currentTime - randomSeed;
    }

    private Buffer wrapBuffer(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[byteBuffer.remaining()];
        byteBuffer.get(buffer);
        return new FixedBuffer(buffer);
    }

    private SpanBo randomSpan() {
        TSpan tSpan = randomTSpan.randomTSpan();
        return spanFactory.buildSpanBo(tSpan, spanAcceptedTime, filter);
    }

    private <T> List<T> newArrayList(T... elements) {
        Objects.requireNonNull(elements, "elements");
        List<T> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public SpanBo randomComplexSpan() {
        TSpan tSpan = randomTSpan.randomTSpan();
        TSpanEvent tSpanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        TSpanEvent tSpanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        TSpanEvent tSpanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        tSpan.setSpanEventList(newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));
        return spanFactory.buildSpanBo(tSpan, spanAcceptedTime, filter);
    }

    private SpanChunkBo randomSpanChunk() {
        TSpanChunk tSpanChunk = randomTSpan.randomTSpanChunk();
        return spanFactory.buildSpanChunkBo(tSpanChunk, spanAcceptedTime, filter);
    }

    public SpanChunkBo randomComplexSpanChunk() {
        TSpanChunk tSpanChunk = randomTSpan.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        TSpanEvent tSpanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        TSpanEvent tSpanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        tSpanChunk.setSpanEventList(newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));
        return spanFactory.buildSpanChunkBo(tSpanChunk, spanAcceptedTime, filter);
    }


    private void assertSpan(SpanBo spanBo) {
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(spanBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanBo.getCollectorAcceptTime());

        SpanBo decode = (SpanBo) spanDecoder.decode(qualifier, column, decodingContext);
        // TODO Check CI log
        // logger.debug("span dump \noriginal spanBo:{} \ndecode spanBo:{} ", spanBo, decode);

        List<String> notSerializedField = newArrayList("parentApplicationId", "parentApplicationServiceType");
        List<String> excludeField = newArrayList("annotationBoList", "spanEventBoList");
        notSerializedField.addAll(excludeField);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(decode, spanBo, notSerializedField));

        logger.debug("{} {}", spanBo.getAnnotationBoList(), decode.getAnnotationBoList());
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(spanBo.getAnnotationBoList(), decode.getAnnotationBoList()), "annotation");

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
        Assertions.assertEquals(spanEventBoList, decodedSpanEventBoList);
    }

    private void assertSpanChunk(SpanChunkBo spanChunkBo) {
        spanChunkBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<>(spanChunkBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanChunkQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanChunkColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(spanChunkBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanChunkBo.getCollectorAcceptTime());

        SpanChunkBo decode = (SpanChunkBo) spanDecoder.decode(qualifier, column, decodingContext);
        // TODO Check CI log
        // logger.debug("spanChunk dump \noriginal spanChunkBo:{} \ndecode spanChunkBo:{} ", spanChunkBo, decode);

        List<String> notSerializedField = newArrayList("endPoint", "serviceType", "applicationServiceType");
        List<String> excludeField = newArrayList("spanEventBoList", "localAsyncId");
        notSerializedField.addAll(excludeField);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(decode, spanChunkBo, notSerializedField));


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
        Assertions.assertEquals(spanEventBoList, decodedSpanEventBoList);

    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_startTimeDelta_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setStartElapsed(spanEventBo0.getStartElapsed());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_depth_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setDepth(spanEventBo0.getDepth());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_service_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setServiceType(spanEventBo0.getServiceType());

        assertSpan(spanBo);
    }
}