/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;

/**
 * The Class ContextInvocationInterceptor.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 * @author jaehong.kim
 */
public class ContextInvocationInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {

    /**
     * Instantiates a new invoke context interceptor.
     *
     * @param traceContext the trace context
     */
    public ContextInvocationInterceptor(final TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        recorder.recordServiceType(JbossConstants.JBOSS_METHOD);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        recorder.recordApiId(apiId);
        recorder.recordException(throwable);
    }

}