/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PDataSource;
import com.navercorp.pinpoint.grpc.trace.PDataSourceList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcDataSourceBoMapper implements GrpcStatMapper {

    public DataSourceBo map(DataPoint point, final PDataSource dataSource) {
        return new DataSourceBo(point,
                dataSource.getId(),
                (short) dataSource.getServiceTypeCode(),
                dataSource.getDatabaseName(),
                dataSource.getUrl(),
                dataSource.getActiveConnectionSize(),
                dataSource.getMaxConnectionSize());
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // datasource
        if (agentStat.hasDataSourceList()) {
            DataPoint point = builder.getDataPoint();
            final PDataSourceList dataSourceList = agentStat.getDataSourceList();

            List<DataSourceBo> dataSourceBoList = new ArrayList<>(dataSourceList.getDataSourceCount());
            for (PDataSource dataSource : dataSourceList.getDataSourceList()) {
                final DataSourceBo dataSourceBo = this.map(point, dataSource);
                dataSourceBoList.add(dataSourceBo);
            }

            final DataSourceListBo dataSourceListBo = new DataSourceListBo(point, dataSourceBoList);
            builder.addPoint(dataSourceListBo);
        }
    }
}