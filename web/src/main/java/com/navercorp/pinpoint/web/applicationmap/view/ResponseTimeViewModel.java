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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class ResponseTimeViewModel implements TimeHistogramViewModel {

    private final String columnName;
    private final List<TimeCount> columnValue;

    public ResponseTimeViewModel(String columnName, List<TimeCount> columnValue) {
        this.columnName = Objects.requireNonNull(columnName, "columnName");
        this.columnValue = Objects.requireNonNull(columnValue, "columnValue");
    }

    @JsonProperty("key")
    public String getColumnName() {
        return columnName;
    }

    @JsonProperty("values")
    public List<TimeCount> getColumnValue() {
        return columnValue;
    }

}
