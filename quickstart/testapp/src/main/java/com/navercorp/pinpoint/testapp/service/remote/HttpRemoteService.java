package com.navercorp.pinpoint.testapp.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author koo.taejin
 */
@Component
public class HttpRemoteService implements RemoteService {

    private final ObjectMapper mapper;

    public HttpRemoteService(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public <R> R get(String url, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createGet(url, new LinkedMultiValueMap<>());
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R get(String url, MultiValueMap<String, String> params, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createGet(url, params);
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R post(String url, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createPost(url, new LinkedMultiValueMap<>());
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R post(String url, MultiValueMap<String, String> params, Class<R> responseType) throws IOException {
        HttpUriRequest httpMethod = createPost(url, params);
        return execute(httpMethod, responseType);
    }

    private HttpGet createGet(String url, MultiValueMap<String, String> params) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(url);

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();

            for (String value : entry.getValue()) {
                uri.addParameter(key, value);
            }
        }

        return new HttpGet(uri.build());
    }

    private HttpPost createPost(String url, MultiValueMap<String, String> params) {
        HttpPost post = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();

            for (String value : entry.getValue()) {
                nvps.add(new BasicNameValuePair(key, value));
            }
        }

        post.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        return post;
    }

    private <R> R execute(HttpUriRequest httpMethod, Class<R> responseType) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpclient.execute(httpMethod);
            HttpEntity entity = response.getEntity();
            return mapper.readValue(entity.getContent(), responseType);
        }
    }
}
