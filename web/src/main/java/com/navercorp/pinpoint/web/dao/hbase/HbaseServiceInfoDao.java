package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.common.server.config.ServiceIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.web.dao.ServiceInfoDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// serviceName -> serviceName
//                serviceInfo
@Repository
public class HbaseServiceInfoDao implements ServiceInfoDao {

    private static final HbaseColumnFamily.ServiceInfo NAME = HbaseColumnFamily.SERVICE_NAME;
    private static final HbaseColumnFamily.ServiceInfo INFO = HbaseColumnFamily.SERVICE_INFO;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> serviceNameMapper;
    private final RowMapper<ServiceInfo> serviceInfoMapper;


    public HbaseServiceInfoDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                               @Qualifier("serviceInfoNameMapper") RowMapper<String> serviceNameMapper,
                               @Qualifier("serviceInfoMapper") RowMapper<ServiceInfo> serviceInfoMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceNameMapper = Objects.requireNonNull(serviceNameMapper, "serviceNameMapper");
        this.serviceInfoMapper = Objects.requireNonNull(serviceInfoMapper, "serviceInfoMapper");
    }

    @Override
    @Cacheable(cacheNames = "serviceNameCache", key = "#serviceId", cacheManager = ServiceIdCacheConfiguration.SERVICE_NAME_CACHE_NAME, unless = "#result == null")
    public String selectServiceName(UUID serviceId) {
        byte[] rowKey = BytesUtils.toBytes(serviceId);

        Get get = new Get(rowKey);
        get.addFamily(NAME.getName());

        TableName serviceInfoTableName = tableNameProvider.getTableName(NAME.getTable());

        return hbaseOperations.get(serviceInfoTableName, get, serviceNameMapper);
    }

    @Override
    public ServiceInfo selectServiceInfo(UUID serviceId) {
        byte[] rowKey = BytesUtils.toBytes(serviceId);

        Get get = new Get(rowKey);

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        return hbaseOperations.get(serviceInfoTableName, get, serviceInfoMapper);
    }

    @Override
    public boolean insertServiceInfoIfNotExists(ServiceInfo serviceInfo) {
        byte[] rowKey = BytesUtils.toBytes(serviceInfo.getServiceId());

        Put put = new Put(rowKey);
        put.addColumn(NAME.getName(), NAME.getName(), BytesUtils.toBytes(serviceInfo.getServiceName()));

        if (serviceInfo.getTags() != null) {
            put.addColumn(INFO.getName(), INFO.getName(), BytesUtils.toBytes(serviceInfo.getTags()));
        }

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(NAME.getName(), NAME.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(serviceInfoTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public void updateServiceInfo(UUID serviceId, Map<String, String> tags) {
        byte[] rowKey = BytesUtils.toBytes(serviceId);

        Put put = new Put(rowKey);
        put.addColumn(INFO.getName(), INFO.getName(), BytesUtils.toBytes(tags));

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        hbaseOperations.put(serviceInfoTableName, put);
    }

    @Override
    @CacheEvict(cacheNames = "serviceNameCache", key = "#serviceId", cacheManager = ServiceIdCacheConfiguration.SERVICE_NAME_CACHE_NAME)
    public void deleteServiceInfo(UUID serviceId) {
        byte[] rowKey = BytesUtils.toBytes(serviceId);
        Delete delete = new Delete(rowKey);

        TableName ServiceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        hbaseOperations.delete(ServiceInfoTableName, delete);
    }

}
