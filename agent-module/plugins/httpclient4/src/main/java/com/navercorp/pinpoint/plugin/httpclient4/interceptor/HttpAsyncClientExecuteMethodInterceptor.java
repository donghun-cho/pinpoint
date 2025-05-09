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
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4PluginConfig;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class HttpAsyncClientExecuteMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public HttpAsyncClientExecuteMethodInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        this.markError = HttpClient4PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4_INTERNAL);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);
    }
}