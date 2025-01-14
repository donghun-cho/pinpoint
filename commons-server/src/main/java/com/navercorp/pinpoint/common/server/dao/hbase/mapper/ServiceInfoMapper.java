package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.id.ServiceInfo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ServiceInfoMapper implements RowMapper<ServiceInfo> {

    private static final HbaseColumnFamily.ServiceInfo NAME = HbaseColumnFamily.SERVICE_NAME;
    private static final HbaseColumnFamily.ServiceInfo INFO = HbaseColumnFamily.SERVICE_INFO;

    public ServiceInfo mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        byte[] rowKey = result.getRow();
        UUID serviceId = BytesUtils.toUUID(rowKey);

        byte[] serviceNameBytes = result.getValue(NAME.getName(), NAME.getName());
        String serviceName = BytesUtils.toString(serviceNameBytes);

        Map<String, String> tags = null;
        byte[] tagsBytes = result.getValue(INFO.getName(), INFO.getName());
        if (tagsBytes != null) {
            tags = BytesUtils.toMap(tagsBytes);
        }
        return new ServiceInfo(serviceId, serviceName, tags);
    }
}
