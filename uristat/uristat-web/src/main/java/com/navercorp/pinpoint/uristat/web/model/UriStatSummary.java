/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.web.model;

import java.util.List;

public class UriStatSummary {
    private String uri;
    private Double totalCount;
    private Double failureCount;
    private Double maxTimeMs;
    private Double avgTimeMs;
    private Double apdex;
    private String version;

    private List<UriStatChartValue> chartValue;

    public UriStatSummary() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public Double getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Double failureCount) {
        this.failureCount = failureCount;
    }

    public Double getMaxTimeMs() {
        return maxTimeMs;
    }

    public void setMaxTimeMs(Double maxTimeMs) {
        this.maxTimeMs = maxTimeMs;
    }

    public Double getAvgTimeMs() {
        return avgTimeMs;
    }

    public void setAvgTimeMs(Double avgTimeMs) {
        this.avgTimeMs = avgTimeMs;
    }

    public Double getApdex() {
        return apdex;
    }

    public void setApdex(Double apdex) {
        this.apdex = apdex;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<UriStatChartValue> getChartValue() {
        return chartValue;
    }

    public void setChartValue(List<UriStatChartValue> chartValue) {
        this.chartValue = chartValue;
    }
}
