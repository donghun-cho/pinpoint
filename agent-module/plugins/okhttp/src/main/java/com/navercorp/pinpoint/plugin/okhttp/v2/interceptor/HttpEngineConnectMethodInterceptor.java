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

package com.navercorp.pinpoint.plugin.okhttp.v2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.v2.ConnectionGetter;
import com.squareup.okhttp.Address;
import com.squareup.okhttp.Connection;

/**
 * @author jaehong.kim
 */
public class HttpEngineConnectMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public HttpEngineConnectMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        this.markError = OkHttpPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL);
        recorder.recordException(markError, throwable);

        if (target instanceof ConnectionGetter) {
            final Connection connection = ((ConnectionGetter) target)._$PINPOINT$_getConnection();
            if (connection != null) {
                final String hostAndPort = getHostAndPort(connection);
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, hostAndPort);
            }
        }
    }

    private String getHostAndPort(Connection connection) {
        final Address address = connection.getRoute().getAddress();
        return HostAndPort.toHostAndPortString(address.getUriHost(), address.getUriPort());
    }
}