package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.config.ServiceIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.web.dao.ServiceIdDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// serviceName -> serviceId
@Repository
public class HbaseServiceIdDao implements ServiceIdDao {

    private static final HbaseColumnFamily.ServiceId ID = HbaseColumnFamily.SERVICE_ID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> serviceIdKeyMapper;
    private final RowMapper<UUID> serviceIdMapper;

    public HbaseServiceIdDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                             @Qualifier("serviceIdNameMapper") RowMapper<String> serviceIdKeyMapper,
                             @Qualifier("serviceIdMapper") RowMapper<UUID> serviceIdMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceIdKeyMapper = Objects.requireNonNull(serviceIdKeyMapper, "serviceIdKeyMapper");
        this.serviceIdMapper = Objects.requireNonNull(serviceIdMapper, "serviceIdMapper");
    }

    @Override
    public List<String> selectAllServiceNames() {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(ID.getName(), ID.getName());
        scan.setFilter(new KeyOnlyFilter());

        TableName ServiceIdTableName = tableNameProvider.getTableName(ID.getTable());
        return hbaseOperations.find(ServiceIdTableName, scan, serviceIdKeyMapper);
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

    @Override
    public boolean insertServiceIdIfNotExists(String serviceName, UUID serviceId) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Put put = new Put(rowKey);
        put.addColumn(ID.getName(), ID.getName(), BytesUtils.toBytes(serviceId));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(ID.getName(), ID.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName serviceIdTableName = tableNameProvider.getTableName(ID.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(serviceIdTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    @CacheEvict(cacheNames = "serviceIdCache", key = "#serviceName", cacheManager = ServiceIdCacheConfiguration.SERVICE_ID_CACHE_NAME)
    public void deleteServiceId(String serviceName) {
        byte[] rowKey = BytesUtils.toBytes(serviceName);

        Delete delete = new Delete(rowKey);

        TableName serviceIdTableName = tableNameProvider.getTableName(ID.getTable());
        hbaseOperations.delete(serviceIdTableName, delete);
    }
}
