package com.hayaan.config;


import io.jsonwebtoken.Header;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


@Service
@Slf4j
public class AsyncHttpConfig {


    private final AsyncHttpClient asyncHttpClient;

    public AsyncHttpConfig() throws SSLException {
        SslContext sc = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setCompressionEnforced(true)
                .setMaxConnections(100)
                .setPooledConnectionIdleTimeout(20000)
                .setRequestTimeout(20000)
                .setMaxConnectionsPerHost(5000)
                .setSslContext(sc)
                .build();

        this.asyncHttpClient = new DefaultAsyncHttpClient(clientConfig);
    }

    public JSONObject sendRequest(RequestBuilder request) {
        AtomicReference<JSONObject> responseBody = new AtomicReference<>(new JSONObject());

        request.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        try {
            CompletableFuture<Response> responseFuture = asyncHttpClient.executeRequest(request).toCompletableFuture();
            Response response = responseFuture.join(); // Wait for completion and get the response

            log.info("Response body: {}", response.getResponseBody());

            try {
                log.info("Received [HttpStatus = {}, Body = {}, URL = {}] processing halted!",
                        response.getStatusCode(), response.getResponseBody(), request);
                responseBody.set(new JSONObject(response.getResponseBody()));
            } catch (Exception e) {
                log.error("JSON Processing failed: [Message= {}]", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Micro-Service Sending Failed: [Message= {}]", e.getMessage());
        }

        return responseBody.get();
    }
}
