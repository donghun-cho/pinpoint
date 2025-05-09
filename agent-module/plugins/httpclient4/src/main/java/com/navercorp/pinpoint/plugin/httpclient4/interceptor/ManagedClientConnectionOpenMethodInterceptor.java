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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient4.EndPointUtils;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4PluginConfig;
import org.apache.http.conn.routing.HttpRoute;

/**
 * @author jaehong.kim
 */
public class ManagedClientConnectionOpenMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public ManagedClientConnectionOpenMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        this.markError = HttpClient4PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        HttpRoute route = ArrayArgumentUtils.getArgument(args, 0, HttpRoute.class);
        if (route != null) {
            final String hostAndPort = EndPointUtils.getHostAndPort(route);
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, hostAndPort);
        }
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(markError, throwable);
    }
}