/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author HyunGil Jeong
 */
public class AgentStatPoint implements Point {

    private final long xVal;
    private final double yVal;

    public AgentStatPoint(long xVal, double yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

    @Override
    public long getXVal() {
        return xVal;
    }


    public double getYVal() {
        return yVal;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatPoint that = (AgentStatPoint) o;
        return xVal == that.xVal && Double.compare(yVal, that.yVal) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(xVal);
        result = 31 * result + Double.hashCode(yVal);
        return result;
    }

    @Override
    public String toString() {
        return "AgentStatPoint{" +
                 xVal + "=" + yVal +
                '}';
    }
}
