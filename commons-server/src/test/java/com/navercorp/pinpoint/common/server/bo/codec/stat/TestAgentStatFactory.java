/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public class TestAgentStatFactory {

    private static final int MAX_NUM_TEST_VALUES = 20 + 1; // Random API's upper bound field is exclusive

    private static final long TIMESTAMP_INTERVAL = 5000L;

    private static final Random RANDOM = new Random();

    private static final UriStatHistogramBucket.Layout layout = UriStatHistogramBucket.getLayout();

    public static List<JvmGcBo> createJvmGcBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createJvmGcBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<JvmGcBo> createJvmGcBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<JvmGcBo> jvmGcBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> heapUseds = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                256 * 1024 * 1024L,
                512 * 1024 * 1024L,
                10 * 1024 * 1024L,
                30 * 1024 * 1024L,
                numValues);
        List<Long> heapMaxes = TestAgentStatDataPointFactory.LONG.createConstantValues(
                2 * 1024 * 1024 * 1024L,
                2 * 1024 * 1024 * 1024L,
                numValues);
        List<Long> nonHeapUseds = TestAgentStatDataPointFactory.LONG.createIncreasingValues(
                16 * 1024 * 1024L,
                64 * 1024 * 1024L,
                1 * 1024 * 1024L,
                3 * 1024 * 1024L,
                numValues);
        List<Long> nonHeapMaxes = TestAgentStatDataPointFactory.LONG.createConstantValues(
                128 * 1024 * 1024L,
                128 * 1024 * 1024L,
                numValues);
        List<Long> gcOldCounts = TestAgentStatDataPointFactory.LONG.createIncreasingValues(
                0L,
                1000L,
                0L,
                10L,
                numValues);
        List<Long> gcOldTimes = TestAgentStatDataPointFactory.LONG.createIncreasingValues(
                0L,
                100000000L,
                100L,
                5000L,
                numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            JvmGcBo jvmGcBo = new JvmGcBo(point);
            jvmGcBo.setGcType(JvmGcType.CMS);
            jvmGcBo.setHeapUsed(heapUseds.get(i));
            jvmGcBo.setHeapMax(heapMaxes.get(i));
            jvmGcBo.setNonHeapUsed(nonHeapUseds.get(i));
            jvmGcBo.setNonHeapMax(nonHeapMaxes.get(i));
            jvmGcBo.setGcOldCount(gcOldCounts.get(i));
            jvmGcBo.setGcOldTime(gcOldTimes.get(i));
            jvmGcBos.add(jvmGcBo);
        }
        return jvmGcBos;
    }

    private static DataPoint newPoint(String agentId, long startTimestamps, long timestamps) {
        return DataPoint.of(agentId, "appName", startTimestamps, timestamps);
    }

    public static List<JvmGcDetailedBo> createJvmGcDetailedBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createJvmGcDetailedBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<JvmGcDetailedBo> createJvmGcDetailedBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<JvmGcDetailedBo> jvmGcDetailedBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> gcNewCounts = TestAgentStatDataPointFactory.LONG.createIncreasingValues(
                0L,
                10000000L,
                10L,
                1000L,
                numValues);
        List<Long> gcNewTimes = TestAgentStatDataPointFactory.LONG.createIncreasingValues(
                0L,
                100L,
                1L,
                50L,
                numValues);
        List<Double> codeCacheUseds = createRandomPercentageValues(numValues);
        List<Double> newGenUseds = createRandomPercentageValues(numValues);
        List<Double> oldGenUseds = createRandomPercentageValues(numValues);
        List<Double> survivorSpaceUseds = createRandomPercentageValues(numValues);
        List<Double> permGenUseds = createRandomPercentageValues(numValues);
        List<Double> metaspaceUseds = createRandomPercentageValues(numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            JvmGcDetailedBo jvmGcDetailedBo = new JvmGcDetailedBo(point);
            jvmGcDetailedBo.setGcNewCount(gcNewCounts.get(i));
            jvmGcDetailedBo.setGcNewTime(gcNewTimes.get(i));
            jvmGcDetailedBo.setCodeCacheUsed(codeCacheUseds.get(i));
            jvmGcDetailedBo.setNewGenUsed(newGenUseds.get(i));
            jvmGcDetailedBo.setOldGenUsed(oldGenUseds.get(i));
            jvmGcDetailedBo.setSurvivorSpaceUsed(survivorSpaceUseds.get(i));
            jvmGcDetailedBo.setPermGenUsed(permGenUseds.get(i));
            jvmGcDetailedBo.setMetaspaceUsed(metaspaceUseds.get(i));
            jvmGcDetailedBos.add(jvmGcDetailedBo);
        }
        return jvmGcDetailedBos;
    }

    public static List<CpuLoadBo> createCpuLoadBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createCpuLoadBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<CpuLoadBo> createCpuLoadBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<CpuLoadBo> cpuLoadBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Double> jvmCpuLoads = createRandomPercentageValues(numValues);
        List<Double> systemCpuLoads = createRandomPercentageValues(numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            CpuLoadBo cpuLoadBo = new CpuLoadBo(point);
            cpuLoadBo.setJvmCpuLoad(jvmCpuLoads.get(i));
            cpuLoadBo.setSystemCpuLoad(systemCpuLoads.get(i));
            cpuLoadBos.add(cpuLoadBo);
        }
        return cpuLoadBos;
    }

    public static List<TransactionBo> createTransactionBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createTransactionBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<TransactionBo> createTransactionBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<TransactionBo> transactionBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> collectIntervals = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> sampledNewCounts = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> sampledContinuationCounts = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> unsampledNewCount = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> unsampledContinuationCount = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> skippedNewCount = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        List<Long> skippedContinuationCount = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(
                100L,
                10000L,
                10L,
                100L,
                numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            TransactionBo transactionBo = new TransactionBo(point);
            transactionBo.setCollectInterval(collectIntervals.get(i));
            transactionBo.setSampledNewCount(sampledNewCounts.get(i));
            transactionBo.setSampledContinuationCount(sampledContinuationCounts.get(i));
            transactionBo.setUnsampledNewCount(unsampledNewCount.get(i));
            transactionBo.setUnsampledContinuationCount(unsampledContinuationCount.get(i));
            transactionBo.setSkippedNewSkipCount(skippedNewCount.get(i));
            transactionBo.setSkippedContinuationCount(skippedContinuationCount.get(i));
            transactionBos.add(transactionBo);
        }
        return transactionBos;
    }

    public static List<ActiveTraceBo> createActiveTraceBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createActiveTraceBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<ActiveTraceBo> createActiveTraceBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<ActiveTraceBo> activeTraceBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Integer> fastTraceCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        List<Integer> normalTraceCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        List<Integer> slowTraceCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        List<Integer> verySlowTraceCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        int histogramSchemaType = 1;
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            ActiveTraceBo activeTraceBo = new ActiveTraceBo(point);
            activeTraceBo.setHistogramSchemaType(histogramSchemaType);
            if (RANDOM.nextInt(5) > 0) {
                ActiveTraceHistogram activeTraceHistogram = newActiveTraceHistogram(fastTraceCounts, normalTraceCounts, slowTraceCounts, verySlowTraceCounts, i);
                activeTraceBo.setActiveTraceHistogram(activeTraceHistogram);
            } else {
                activeTraceBo.setActiveTraceHistogram(ActiveTraceHistogram.UNCOLLECTED);
            }
            activeTraceBos.add(activeTraceBo);
        }
        return activeTraceBos;
    }

    private static ActiveTraceHistogram newActiveTraceHistogram(List<Integer> fastTraceCounts, List<Integer> normalTraceCounts, List<Integer> slowTraceCounts, List<Integer> verySlowTraceCounts, int index) {
        int fast = fastTraceCounts.get(index);
        int normal = normalTraceCounts.get(index);
        int slow = slowTraceCounts.get(index);
        int verySlow = verySlowTraceCounts.get(index);
        return new ActiveTraceHistogram(fast, normal, slow, verySlow);
    }

    public static List<ResponseTimeBo> createResponseTimeBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createResponseTimeBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<ResponseTimeBo> createResponseTimeBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<ResponseTimeBo> responseTimeBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> avgs = TestAgentStatDataPointFactory.LONG.createRandomValues(0L, 1000L, numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            ResponseTimeBo responseTimeBo = new ResponseTimeBo(point);
            responseTimeBo.setAvg(avgs.get(i));
            responseTimeBos.add(responseTimeBo);
        }
        return responseTimeBos;
    }

    public static List<DeadlockThreadCountBo> createDeadlockBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createDeadlockBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<DeadlockThreadCountBo> createDeadlockBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<DeadlockThreadCountBo> deadlockThreadCountBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Integer> deadlockCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            DeadlockThreadCountBo deadlockThreadCountBo = new DeadlockThreadCountBo(point);
            deadlockThreadCountBo.setDeadlockedThreadCount(deadlockCounts.get(i));

            deadlockThreadCountBos.add(deadlockThreadCountBo);
        }
        return deadlockThreadCountBos;
    }

    private static final int MIN_VALUE_OF_MAX_CONNECTION_SIZE = 20;

    public static List<DataSourceListBo> createDataSourceListBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createDataSourceListBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<DataSourceListBo> createDataSourceListBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<DataSourceListBo> dataSourceListBos = new ArrayList<>(numValues);

        for (int i = 0; i < numValues; i++) {
            int maxConnectionSize = RANDOM.nextInt(MIN_VALUE_OF_MAX_CONNECTION_SIZE, MIN_VALUE_OF_MAX_CONNECTION_SIZE * 2);
            int dataSourceBoSize = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
            DataSourceListBo dataSourceListBo = createDataSourceListBo(agentId, startTimestamp, initialTimestamp, i + 1, maxConnectionSize, dataSourceBoSize);
            dataSourceListBos.add(dataSourceListBo);
        }
        return dataSourceListBos;
    }

    private static DataSourceListBo createDataSourceListBo(String agentId, long startTimestamp, long initialTimestamp, int id, int maxConnectionSize, int numValues) {
        DataPoint point = newPoint(agentId, startTimestamp, initialTimestamp);

        DataSourceListBo dataSourceListBo = new DataSourceListBo(point);

        for (int i = 0; i < numValues; i++) {
            DataSourceBo dataSourceBo = new DataSourceBo(point);

            dataSourceBo.setId(id);
            dataSourceBo.setServiceTypeCode(ServiceType.UNKNOWN.getCode());
            dataSourceBo.setDatabaseName("name-" + id);
            dataSourceBo.setJdbcUrl("jdbcurl-" + id);
            dataSourceBo.setActiveConnectionSize(RANDOM.nextInt(maxConnectionSize));
            dataSourceBo.setMaxConnectionSize(maxConnectionSize);

            dataSourceListBo.add(dataSourceBo);
        }

        return dataSourceListBo;
    }

    public static List<FileDescriptorBo> createFileDescriptorBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createFileDescriptorBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<FileDescriptorBo> createFileDescriptorBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<FileDescriptorBo> fileDescriptorBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> openFileDescriptors = TestAgentStatDataPointFactory.LONG.createRandomValues(1L, 10000L, numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            FileDescriptorBo fileDescriptorBo = new FileDescriptorBo(point);
            fileDescriptorBo.setOpenFileDescriptorCount(openFileDescriptors.get(i));
            fileDescriptorBos.add(fileDescriptorBo);
        }
        return fileDescriptorBos;
    }

    public static List<DirectBufferBo> createDirectBufferBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createDirectBufferBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<DirectBufferBo> createDirectBufferBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<DirectBufferBo> directBufferBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);
        List<Long> directBuffers1 = TestAgentStatDataPointFactory.LONG.createRandomValues(1L, 10000L, numValues);
        List<Long> directBuffers2 = TestAgentStatDataPointFactory.LONG.createRandomValues(1L, 10000L, numValues);
        List<Long> directBuffers3 = TestAgentStatDataPointFactory.LONG.createRandomValues(1L, 10000L, numValues);
        List<Long> directBuffers4 = TestAgentStatDataPointFactory.LONG.createRandomValues(1L, 10000L, numValues);

        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            DirectBufferBo directBufferBo = new DirectBufferBo(point);
            directBufferBo.setDirectCount(directBuffers1.get(i));
            directBufferBo.setDirectMemoryUsed(directBuffers2.get(i));
            directBufferBo.setMappedCount(directBuffers3.get(i));
            directBufferBo.setMappedMemoryUsed(directBuffers4.get(i));

            directBufferBos.add(directBufferBo);
        }
        return directBufferBos;
    }

    public static List<TotalThreadCountBo> createTotalThreadCountBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createTotalThreadCountBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<TotalThreadCountBo> createTotalThreadCountBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<TotalThreadCountBo> totalThreadCountBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);

        List<Integer> totalThreadCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            TotalThreadCountBo totalThreadCountBo = new TotalThreadCountBo(point);
            totalThreadCountBo.setTotalThreadCount(totalThreadCounts.get(i));

            totalThreadCountBos.add(totalThreadCountBo);
        }
        return totalThreadCountBos;
    }

    public static List<LoadedClassBo> createLoadedClassBos(String agentId, long startTimestamp, long initialTimestamp) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createLoadedClassBos(agentId, startTimestamp, initialTimestamp, numValues);
    }

    public static List<LoadedClassBo> createLoadedClassBos(String agentId, long startTimestamp, long initialTimestamp, int numValues) {
        List<LoadedClassBo> loadedClassBos = new ArrayList<>(numValues);
        List<Long> startTimestamps = createStartTimestamps(startTimestamp, numValues);
        List<Long> timestamps = createTimestamps(initialTimestamp, numValues);

        List<Integer> loadedClassCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        List<Integer> unloadedClassCounts = TestAgentStatDataPointFactory.INTEGER.createRandomValues(0, 1000, numValues);
        for (int i = 0; i < numValues; i++) {
            DataPoint point = newPoint(agentId, startTimestamps.get(i), timestamps.get(i));

            LoadedClassBo loadedClassBo = new LoadedClassBo(point);
            loadedClassBo.setLoadedClassCount(loadedClassCounts.get(i));
            loadedClassBo.setUnloadedClassCount(unloadedClassCounts.get(i));

            loadedClassBos.add(loadedClassBo);
        }
        return loadedClassBos;
    }

    public static List<AgentUriStatBo> createAgentUriStatBo(String agentId) {
        final int numValues = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);
        return createAgentUriStatBo(agentId, numValues);
    }

    private static List<AgentUriStatBo> createAgentUriStatBo(String agentId, int numValues) {
        AgentUriStatBo agentUriStatBo = new AgentUriStatBo();
        agentUriStatBo.setAgentId(agentId);
        agentUriStatBo.setBucketVersion(layout.getBucketVersion());

        List<EachUriStatBo> eachUriStatBoList = createEachUriStatBoList(numValues);
        agentUriStatBo.setEachUriStatBoList(eachUriStatBoList);

        return List.of(agentUriStatBo);
    }

    private static List<EachUriStatBo> createEachUriStatBoList(int numValues) {
        List<EachUriStatBo> result = new ArrayList<>();

        for (int i = 0; i < numValues; i++) {
            final int requestCount = RANDOM.nextInt(1, MAX_NUM_TEST_VALUES);

            boolean includeFail = RANDOM.nextBoolean();
            EachUriStatBo eachUriStatBo = createEachUriStatBo("/index" + i + ".html", requestCount, includeFail);
            result.add(eachUriStatBo);
        }

        return result;
    }

    private static EachUriStatBo createEachUriStatBo(String uri, int requestCount, boolean includeFail) {
        int[] elapsedTimes = new int[requestCount];
        for (int i = 0; i < requestCount; i++) {
            final int elapsedTime = RANDOM.nextInt(1, 10000);
            elapsedTimes[i] = elapsedTime;
        }

        EachUriStatBo eachUriStatBo = new EachUriStatBo();
        eachUriStatBo.setUri(uri);

        UriStatHistogram total = createHistogram(elapsedTimes, 1);
        eachUriStatBo.setTotalHistogram(total);

        if (includeFail) {
            UriStatHistogram fail = createHistogram(elapsedTimes, 3);
            eachUriStatBo.setFailedHistogram(fail);
        }
        eachUriStatBo.setTimestamp(System.currentTimeMillis());
        return eachUriStatBo;
    }

    private static UriStatHistogram createHistogram(int[] elapsedTimes, int sample) {
        UriStatHistogram uriStatHistogram = new UriStatHistogram();

        int count = 0;
        long totalElapsed = 0;
        long max = 0;
        int histogramSize = UriStatHistogramBucket.values().length;
        int[] histogramBucket = new int[histogramSize];
        for (int elapsedTime : elapsedTimes) {
            if (RANDOM.nextInt(0, sample) != 0) {
                continue;
            }

            totalElapsed += elapsedTime;
            max = Math.max(max, elapsedTime);
            count++;

            UriStatHistogramBucket value = layout.getBucket(elapsedTime);
            histogramBucket[value.getIndex()] += 1;
        }

        if (count == 0) {
            return null;
        }

        uriStatHistogram.setTotal(totalElapsed);
        uriStatHistogram.setMax(max);

        uriStatHistogram.setTimestampHistogram(histogramBucket);

        return uriStatHistogram;
    }

    private static List<Long> createStartTimestamps(long startTimestamp, int numValues) {
        return TestAgentStatDataPointFactory.LONG.createConstantValues(startTimestamp, startTimestamp, numValues);
    }

    private static List<Long> createTimestamps(long initialTimestamp, int numValues) {
        long minTimestampInterval = TIMESTAMP_INTERVAL - 5L;
        long maxTimestampInterval = TIMESTAMP_INTERVAL + 5L;
        return TestAgentStatDataPointFactory.LONG.createIncreasingValues(initialTimestamp, initialTimestamp, minTimestampInterval, maxTimestampInterval, numValues);
    }

    private static List<Double> createRandomPercentageValues(int numValues) {
        List<Double> values = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            int randomInt = RANDOM.nextInt(101);
            double value = randomInt;
            if (randomInt != 100) {
                value += RANDOM.nextDouble();
            }
            values.add(value);
        }
        return values;
    }


}
