/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.okhttp;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

public abstract class OkHttpClient_3_4_0_BaseIT {
    static final String ASYNC = "ASYNC";
    static final String OK_HTTP_CLIENT = "OK_HTTP_CLIENT";
    static final String OK_HTTP_CLIENT_INTERNAL = "OK_HTTP_CLIENT_INTERNAL";

    @AutoClose("stop")
    private static WebServer webServer;

    @BeforeAll
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @Test
    public void execute() throws Exception {
        Request request = new Request.Builder().url(webServer.getCallHttpUrl()).build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method executeMethod = Class.forName("okhttp3.RealCall").getDeclaredMethod("execute");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, executeMethod));

        Method interceptMethod = Class.forName("okhttp3.internal.http.BridgeInterceptor").getDeclaredMethod("intercept", Class.forName("okhttp3.Interceptor$Chain"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT, interceptMethod,
                null, null, webServer.getHostAndPort(),
                annotation("http.url", request.url().toString()),
                annotation("http.status.code", response.code()))
        );

        String hostAndPort = toHostAndPortString(request.url().host(), request.url().port());
        Method connectMethod = getConnectMethod(Class.forName("okhttp3.internal.connection.RealConnection"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, connectMethod,
                annotation("http.internal.display", hostAndPort)));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void enqueue() throws Exception {
        Request request = new Request.Builder().url(webServer.getCallHttpUrl()).build();
        OkHttpClient client = new OkHttpClient();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response> responseRef = new AtomicReference<>(null);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) {
                responseRef.set(response);
                latch.countDown();
            }
        });
        latch.await(3, TimeUnit.SECONDS);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(event(ASYNC, "Asynchronous Invocation"), 20, 3000);
        verifier.printCache();

        Method realCallEnqueueMethod = Class.forName("okhttp3.RealCall").getDeclaredMethod("enqueue", Class.forName("okhttp3.Callback"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, realCallEnqueueMethod));

        Method dispatcherEnqueueMethod = Class.forName("okhttp3.Dispatcher").getDeclaredMethod("enqueue", Class.forName("okhttp3.RealCall$AsyncCall"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, dispatcherEnqueueMethod));

        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));

        Method executeMethod = Class.forName("okhttp3.RealCall$AsyncCall").getDeclaredMethod("execute");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, executeMethod));

        Response response = responseRef.get();
        Method interceptMethod = Class.forName("okhttp3.internal.http.BridgeInterceptor").getDeclaredMethod("intercept", Class.forName("okhttp3.Interceptor$Chain"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT, interceptMethod,
                null, null, webServer.getHostAndPort(),
                annotation("http.url", request.url().toString()),
                annotation("http.status.code", response.code()))
        );

        String hostAndPort = toHostAndPortString(request.url().host(), request.url().port());
        Method connectMethod = getConnectMethod(Class.forName("okhttp3.internal.connection.RealConnection"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, connectMethod,
                annotation("http.internal.display", hostAndPort)));

        verifier.verifyTraceCount(0);
    }

    private Method getConnectMethod(Class<?> realConnectionClass) throws ClassNotFoundException {
        // [3.4.0,3.5.max]
        try {
            return realConnectionClass.getDeclaredMethod("connect", int.class, int.class, int.class, List.class, boolean.class);
        } catch (NoSuchMethodException e) {
            try {
                // [3.6.0,3.8.max]
                return realConnectionClass.getDeclaredMethod("connect", int.class, int.class, int.class, boolean.class);
            } catch (NoSuchMethodException e1) {
                try {
                    // [3.9.0,3.9.max]
                    return realConnectionClass.getDeclaredMethod("connect", int.class, int.class, int.class, boolean.class,
                            Class.forName("okhttp3.Call"), Class.forName("okhttp3.EventListener"));
                } catch (NoSuchMethodException e2) {
                    try {
                        // [3.10.0,)
                        return realConnectionClass.getDeclaredMethod("connect", int.class, int.class, int.class, int.class, boolean.class,
                                Class.forName("okhttp3.Call"), Class.forName("okhttp3.EventListener"));
                    } catch (NoSuchMethodException e3) {
                        throw new AssertionError("Expected methods connect(...) not found in RealConnection class");
                    }
                }
            }
        }
    }

    public static String toHostAndPortString(String host, int port) {
        return toHostAndPortString(host, port, -1);
    }

    /**
     * This API does not verification for input args.
     */
    public static String toHostAndPortString(String host, int port, int noPort) {
        // don't validation hostName
        // don't validation port range
        if (noPort == port) {
            return host;
        }
        return host + ':' + port;
    }
}
