/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author Roy Kim
 */
public class FileDescriptorBo extends AgentStatDataBasePoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private long openFileDescriptorCount = UNCOLLECTED_VALUE;


    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.FILE_DESCRIPTOR;
    }

    public long getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    public void setOpenFileDescriptorCount(long openFileDescriptorCount) {
        this.openFileDescriptorCount = openFileDescriptorCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileDescriptorBo fileDescriptorBo = (FileDescriptorBo) o;

        if (startTimestamp != fileDescriptorBo.startTimestamp) return false;
        if (timestamp != fileDescriptorBo.timestamp) return false;
        if (openFileDescriptorCount != fileDescriptorBo.openFileDescriptorCount) return false;
        return agentId != null ? agentId.equals(fileDescriptorBo.agentId) : fileDescriptorBo.agentId == null;

    }

    @Override
    public int hashCode() {
        int result;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + Long.hashCode(startTimestamp);
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + Long.hashCode(openFileDescriptorCount);
        return result;
    }

    @Override
    public String toString() {
        return "FileDescriptorBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", openFileDescriptorCount=" + openFileDescriptorCount +
                '}';
    }
}
