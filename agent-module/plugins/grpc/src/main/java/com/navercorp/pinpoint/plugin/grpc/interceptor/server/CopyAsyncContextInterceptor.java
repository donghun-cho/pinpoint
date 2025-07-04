/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc.interceptor.server;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.ServerStreamGetter;
import io.grpc.ForwardingServerCall;
import io.grpc.internal.ServerStream;

import java.lang.reflect.Method;

/**
 * @author Taejin Koo
 * @author shofee
 */
public class CopyAsyncContextInterceptor implements AroundInterceptor {
    private static final int MAX_DELEGATE_CALLS = 10;
    private final PluginLogger logger = PluginLogManager.getLogger(CopyAsyncContextInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    public CopyAsyncContextInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        if (ArrayUtils.getLength(args) == 2) {
            AsyncContext asyncContext = getAsyncContext(args[0]);

            if (result instanceof AsyncContextAccessor) {
                logger.info("set AsyncContext:{}", asyncContext);
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }

    AsyncContext getAsyncContext(Object object) {
        Object target = object;
        try {
            int count = 0;
            while (!(target instanceof ServerStreamGetter) && (target instanceof ForwardingServerCall)) {
                Method delegateMethod = ForwardingServerCall.class.getDeclaredMethod("delegate");
                delegateMethod.setAccessible(true);
                target = delegateMethod.invoke(target);
                if (count++ >= MAX_DELEGATE_CALLS) {
                    return null;
                }
            }
        } catch (Exception ignored) {
            return null;
        }

        if (object instanceof ServerStreamGetter) {
            ServerStream serverStream = ((ServerStreamGetter) object)._$PINPOINT$_getServerStream();
            if (serverStream instanceof AsyncContextAccessor) {
                return ((AsyncContextAccessor) serverStream)._$PINPOINT$_getAsyncContext();
            }
        }
        return null;
    }
}
