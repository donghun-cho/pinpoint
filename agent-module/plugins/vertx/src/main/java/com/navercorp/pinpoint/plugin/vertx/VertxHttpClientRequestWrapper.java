/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientRequestWrapper implements ClientRequestWrapper {
    private final HttpRequest httpRequest;
    private final String host;

    public VertxHttpClientRequestWrapper(final HttpRequest httpRequest, final String host) {
        this.httpRequest = Objects.requireNonNull(httpRequest, "httpRequest");
        this.host = host;
    }


    @Override
    public String getDestinationId() {
        if (this.host != null) {
            return this.host;
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        return getHttpUrl(this.host, getUri());
    }

    @SuppressWarnings("deprecation")
    private String getUri() {
        return this.httpRequest.getUri();
    }

    private static String getHttpUrl(final String host, final String uri) {
        if (host == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (uri != null) {
            sb.append(uri);
        }
        return sb.toString();
    }
}
