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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.hbase.rowmapper.RequestAwareDynamicRowMapper;
import com.navercorp.pinpoint.common.hbase.rowmapper.RequestAwareRowMapper;
import com.navercorp.pinpoint.common.hbase.rowmapper.RequestAwareRowMapperAdaptor;
import com.navercorp.pinpoint.common.hbase.rowmapper.ResultSizeMapper;
import com.navercorp.pinpoint.common.hbase.rowmapper.RowMapperResultAdaptor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoder;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.mapper.SpanMapperFactory;
import com.navercorp.pinpoint.web.service.FetchResult;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Woonduk Kang(emeroad)
 * @author Taejin Koo
 */
@Repository
public class HbaseTraceDaoV2 implements TraceDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.TRACE_V2_SPAN;

    private final HbaseOperations template2;
    private final TableNameProvider tableNameProvider;

    private final RowKeyEncoder<TransactionId> rowKeyEncoder;

    private final SpanMapperFactory spanMapperFactory;

    @Value("${web.hbase.selectSpans.limit:500}")
    private int selectSpansLimit;

    @Value("${web.hbase.selectAllSpans.limit:500}")
    private int selectAllSpansLimit;

    @Value("${web.hbase.trace.max.results.limit:50000}")
    private int traceMaxResultsPerColumnFamily;

    private final Filter spanFilter = createSpanQualifierFilter();

    public HbaseTraceDaoV2(HbaseOperations template2,
                           TableNameProvider tableNameProvider,
                           @Qualifier("traceRowKeyEncoderV2") RowKeyEncoder<TransactionId> rowKeyEncoder,
                           SpanMapperFactory spanMapperFactory) {
        this.template2 = Objects.requireNonNull(template2, "template2");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.spanMapperFactory = Objects.requireNonNull(spanMapperFactory, "spanMapperFactory");
    }

    @Override
    public List<SpanBo> selectSpan(TransactionId transactionId) {
        return selectSpan(transactionId, null).data();
    }

    @Override
    public FetchResult<List<SpanBo>> selectSpan(TransactionId transactionId, ColumnGetCount columnGetCount) {
        Objects.requireNonNull(transactionId, "transactionId");

        byte[] transactionIdRowKey = rowKeyEncoder.encodeRowKey(transactionId);

        final Get get = new Get(transactionIdRowKey);
        get.setMaxResultsPerColumnFamily(traceMaxResultsPerColumnFamily);
        get.addFamily(DESCRIPTOR.getName());
        final Filter filter = ColumnGetCount.toFilter(columnGetCount);
        if (filter != null) {
            get.setFilter(filter);
        }

        TableName traceTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        ResultSizeMapper<List<SpanBo>> resultSizeMapper = new ResultSizeMapper<>();
        RowMapper<List<SpanBo>> rowMapper = new RowMapperResultAdaptor<>(spanMapperFactory.getSpanMapper(), resultSizeMapper);

        List<SpanBo> spanBos = template2.get(traceTableName, get, rowMapper);

        return new FetchResult<>(resultSizeMapper.getResultSize(), spanBos);
    }

    @Override
    public List<List<SpanBo>> selectSpans(List<GetTraceInfo> getTraceInfoList) {
        return selectSpans(getTraceInfoList, selectSpansLimit);
    }

    List<List<SpanBo>> selectSpans(List<GetTraceInfo> getTraceInfoList, int eachPartitionSize) {
        if (CollectionUtils.isEmpty(getTraceInfoList)) {
            return Collections.emptyList();
        }
        List<SpanQuery> spanQuery = getTraceInfoList.stream()
                .map(this::toSpanQuery)
                .collect(Collectors.toList());
        List<List<SpanQuery>> partitionGetTraceInfoList = partition(spanQuery, eachPartitionSize);
        return partitionSelect(partitionGetTraceInfoList, DESCRIPTOR.getName(), spanFilter);
    }

    private SpanQuery toSpanQuery(GetTraceInfo getTraceInfo) {
        SpanQueryBuilder builder = new SpanQueryBuilder();
        return builder.build(getTraceInfo);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList) {
        return selectAllSpans(transactionIdList, selectAllSpansLimit, null);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList, ColumnGetCount columnGetCount) {
        Filter filter = ColumnGetCount.toFilter(columnGetCount);
        return selectAllSpans(transactionIdList, selectAllSpansLimit, filter);
    }

    List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList, int eachPartitionSize, Filter filter) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        List<SpanQuery> getTraceInfoList = transactionIdList.stream()
                .map(SpanQuery::new)
                .collect(Collectors.toList());

        List<List<SpanQuery>> partitionGetTraceInfoList = partition(getTraceInfoList, eachPartitionSize);
        return partitionSelect(partitionGetTraceInfoList, DESCRIPTOR.getName(), filter);
    }

    private List<List<SpanQuery>> partition(List<SpanQuery> getTraceInfoList, int maxTransactionIdListSize) {
        return ListUtils.partition(getTraceInfoList, maxTransactionIdListSize);
    }

    private List<List<SpanBo>> partitionSelect(List<List<SpanQuery>> partitionGetTraceInfoList, byte[] columnFamily, Filter filter) {
        if (CollectionUtils.isEmpty(partitionGetTraceInfoList)) {
            return Collections.emptyList();
        }
        Objects.requireNonNull(columnFamily, "columnFamily");

        List<List<SpanBo>> spanBoList = new ArrayList<>();
        for (List<SpanQuery> getTraceInfoList : partitionGetTraceInfoList) {
            List<List<SpanBo>> result = bulkSelect(getTraceInfoList, columnFamily, filter);
            spanBoList.addAll(result);
        }
        return spanBoList;
    }

    private List<List<SpanBo>> bulkSelect(List<SpanQuery> getTraceInfoList, byte[] columnFamily, Filter filter) {
        if (CollectionUtils.isEmpty(getTraceInfoList)) {
            return Collections.emptyList();
        }
        Objects.requireNonNull(columnFamily, "columnFamily");

        List<Get> getList = createGetList(getTraceInfoList, columnFamily, filter);

        RowMapper<List<SpanBo>> spanMapperAdaptor = newRowMapper(getTraceInfoList);
        return bulkSelect0(getList, spanMapperAdaptor);
    }

    private RowMapper<List<SpanBo>> newRowMapper(List<SpanQuery> spanQueryList) {
        RequestAwareRowMapper<List<SpanBo>, SpanQuery> getTraceInfoRowMapper = new RequestAwareDynamicRowMapper<>(this::getSpanMapper);
        return new RequestAwareRowMapperAdaptor<>(spanQueryList, getTraceInfoRowMapper);
    }


    private RowMapper<List<SpanBo>> getSpanMapper(SpanQuery spanQuery) {
        return spanMapperFactory.getSpanMapper(spanQuery.getSpanFilter());
    }

    private List<Get> createGetList(List<SpanQuery> spanQueryList, byte[] columnFamily, Filter defaultFilter) {
        if (CollectionUtils.isEmpty(spanQueryList)) {
            return Collections.emptyList();
        }
        final List<Get> getList = new ArrayList<>(spanQueryList.size());
        for (SpanQuery spanQuery : spanQueryList) {
            Filter spanQueryFilter = spanQuery.getHbaseFilter();
            Filter filter = HBaseUtils.newFilterList(defaultFilter, spanQueryFilter);

            final Get get = createGet(spanQuery.getTransactionId(), columnFamily, filter);
            getList.add(get);
        }
        return getList;
    }


    private List<List<SpanBo>> bulkSelect0(List<Get> multiGet, RowMapper<List<SpanBo>> rowMapperList) {
        if (CollectionUtils.isEmpty(multiGet)) {
            return Collections.emptyList();
        }

        TableName traceTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return template2.get(traceTableName, multiGet, rowMapperList);
    }

    private Get createGet(TransactionId transactionId, byte[] columnFamily, Filter filter) {
        byte[] transactionIdRowKey = rowKeyEncoder.encodeRowKey(transactionId);
        final Get get = new Get(transactionIdRowKey);
        get.setMaxResultsPerColumnFamily(traceMaxResultsPerColumnFamily);
        get.addFamily(columnFamily);
        if (filter != null) {
            get.setFilter(filter);
        }
        return get;
    }

    public Filter createSpanQualifierFilter() {
        byte indexPrefix = SpanEncoder.TYPE_SPAN;
        ByteArrayComparable prefixComparator = new BinaryPrefixComparator(new byte[]{indexPrefix});
        return new QualifierFilter(CompareOperator.EQUAL, prefixComparator);
    }


}
