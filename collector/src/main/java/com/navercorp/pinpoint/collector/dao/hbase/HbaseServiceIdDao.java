package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ServiceIdDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.config.ServiceIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

@Repository
public class HbaseServiceIdDao implements ServiceIdDao {

    private static final HbaseColumnFamily.ServiceId ID = HbaseColumnFamily.SERVICE_ID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<UUID> serviceIdMapper;

    public HbaseServiceIdDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                             @Qualifier("serviceIdMapper") RowMapper<UUID> serviceIdMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceIdMapper = Objects.requireNonNull(serviceIdMapper, "serviceIdMapper");
    }

    @Override
    @Cacheable(cacheNames = "serviceIdCache", key = "#serviceName", cacheManager = ServiceIdCacheConfiguration.SERVICE_ID_CACHE_NAME, unless = "#result == null")
    public UUID selectServiceId(String serviceName) {
        byte[] rowKey = BytesUtils.toBytes(serviceName);

        Get get = new Get(rowKey);
        get.addColumn(ID.getName(), ID.getName());

        TableName serviceIdTableName = tableNameProvider.getTableName(ID.getTable());
        return hbaseOperations.get(serviceIdTableName, get, serviceIdMapper);
    }
}
