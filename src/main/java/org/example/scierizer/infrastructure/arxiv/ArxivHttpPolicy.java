package org.example.scierizer.infrastructure.arxiv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ArxivHttpPolicy {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final long MIN_REQUEST_INTERVAL_MILLIS = Duration.ofSeconds(3).toMillis();
    private static final String USER_AGENT = "Scierizer/1.0 (arXiv article summarizer)";

    private static long lastArxivRequestStartedAtMillis = 0L;

    private ArxivHttpPolicy() {
    }

    public static HttpRequest.Builder newRequestBuilder(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(REQUEST_TIMEOUT);

        if (isArxivUri(uri)) {
            builder.header("User-Agent", USER_AGENT);
        }

        return builder;
    }

    public static <T> HttpResponse<T> send(
            HttpClient httpClient,
            HttpRequest request,
            HttpResponse.BodyHandler<T> bodyHandler
    ) throws IOException, InterruptedException {
        if (isArxivUri(request.uri())) {
            waitForArxivSlot();
        }

        return httpClient.send(request, bodyHandler);
    }

    public static boolean isArxivUri(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        String normalizedHost = host.toLowerCase();
        return normalizedHost.equals("arxiv.org") || normalizedHost.endsWith(".arxiv.org");
    }

    private static synchronized void waitForArxivSlot() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nextAllowedRequestAt = lastArxivRequestStartedAtMillis + MIN_REQUEST_INTERVAL_MILLIS;

        if (now < nextAllowedRequestAt) {
            Thread.sleep(nextAllowedRequestAt - now);
            now = System.currentTimeMillis();
        }

        lastArxivRequestStartedAtMillis = now;
    }

    static synchronized void resetForTesting() {
        lastArxivRequestStartedAtMillis = 0L;
    }
}
