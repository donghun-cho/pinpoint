/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataEncoder;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseTables.ApiMetadata description = HbaseTables.API_METADATA_API;

    private final HbaseOperations hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final RowKeyEncoder<MetaDataRowKey> rowKeyEncoder = new MetadataEncoder();

    public HbaseApiMetaDataDao(HbaseOperations hbaseTemplate,
                               TableNameProvider tableNameProvider,
                               @Qualifier("metadataRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public void insert(ApiMetaDataBo apiMetaData) {
        Objects.requireNonNull(apiMetaData, "apiMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", apiMetaData);
        }

        final byte[] rowKey = getDistributedKey(rowKeyEncoder.encodeRowKey(apiMetaData));
        final Put put = new Put(rowKey, true);
        final Buffer buffer = new AutomaticBuffer(64);
        final String api = apiMetaData.getApiInfo();
        buffer.putPrefixedString(api);
        buffer.putInt(apiMetaData.getLineNumber());
        buffer.putInt(apiMetaData.getMethodTypeEnum().getCode());
        buffer.putPrefixedString(apiMetaData.getLocation());

        final byte[] apiMetaDataBytes = buffer.getBuffer();
        put.addColumn(description.getName(), description.QUALIFIER_SIGNATURE, apiMetaDataBytes);

        final TableName apiMetaDataTableName = tableNameProvider.getTableName(description.getTable());
        hbaseTemplate.put(apiMetaDataTableName, put);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}