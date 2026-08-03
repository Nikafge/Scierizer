package org.example.summarizer.infrastructure.arxiv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArxivHttpPolicyTest {

    @BeforeEach
    void resetRateLimiter() {
        ArxivHttpPolicy.resetForTesting();
    }

    @Test
    void addsUserAgentOnlyForArxivRequests() {
        HttpRequest arxivRequest = ArxivHttpPolicy.newRequestBuilder(URI.create("https://export.arxiv.org/api/query"))
                .GET()
                .build();
        HttpRequest nonArxivRequest = ArxivHttpPolicy.newRequestBuilder(URI.create("https://example.com/api/query"))
                .GET()
                .build();

        assertTrue(arxivRequest.headers().firstValue("User-Agent").isPresent());
        assertFalse(nonArxivRequest.headers().firstValue("User-Agent").isPresent());
    }

    @Test
    void waitsAtLeastThreeSecondsBetweenArxivRequests() throws Exception {
        FakeHttpClient httpClient = new FakeHttpClient();
        HttpRequest request = ArxivHttpPolicy.newRequestBuilder(URI.create("https://arxiv.org/pdf/2401.00001"))
                .GET()
                .build();

        ArxivHttpPolicy.send(httpClient, request, HttpResponse.BodyHandlers.ofString());
        long firstRequestAt = httpClient.requestTimes.get(0);

        ArxivHttpPolicy.send(httpClient, request, HttpResponse.BodyHandlers.ofString());
        long secondRequestAt = httpClient.requestTimes.get(1);

        assertTrue(secondRequestAt - firstRequestAt >= 2900);
    }

    private static class FakeHttpClient extends HttpClient {
        private final List<Long> requestTimes = new java.util.ArrayList<>();

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException, InterruptedException {
            requestTimes.add(System.currentTimeMillis());
            return new FakeHttpResponse<>(request, null);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return CompletableFuture.completedFuture(new FakeHttpResponse<>(request, null));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return CompletableFuture.completedFuture(new FakeHttpResponse<>(request, null));
        }

        @Override
        public WebSocket.Builder newWebSocketBuilder() {
            return new FakeWebSocketBuilder();
        }
    }

    private record FakeHttpResponse<T>(HttpRequest request, T body) implements HttpResponse<T> {

        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (key, value) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    private static class FakeWebSocketBuilder implements WebSocket.Builder {

        @Override
        public WebSocket.Builder header(String name, String value) {
            return this;
        }

        @Override
        public WebSocket.Builder connectTimeout(Duration timeout) {
            return this;
        }

        @Override
        public WebSocket.Builder subprotocols(String mostPreferred, String... lesserPreferred) {
            return this;
        }

        @Override
        public CompletableFuture<WebSocket> buildAsync(URI uri, WebSocket.Listener listener) {
            return CompletableFuture.completedFuture(new FakeWebSocket());
        }
    }

    private static class FakeWebSocket implements WebSocket {

        @Override
        public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public void request(long n) {
        }

        @Override
        public String getSubprotocol() {
            return "";
        }

        @Override
        public boolean isOutputClosed() {
            return false;
        }

        @Override
        public boolean isInputClosed() {
            return false;
        }

        @Override
        public void abort() {
        }
    }
}
