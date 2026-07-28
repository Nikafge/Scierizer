package org.example.summarizer.infrastructure.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;
import org.example.summarizer.viewmodel.SummaryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaClientSettingsTest {

    private HttpServer server;
    private FakeOllamaHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        handler = new FakeOllamaHandler();
        server.createContext("/api/generate", handler);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void generateSimpleSummaryUsesModelSettingsAndProcessingInstructions() throws IOException {
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Ollama",
                "llama3.1:8b",
                "http://localhost:" + server.getAddress().getPort(),
                "Auto",
                "",
                "",
                false,
                8192,
                1024,
                2
        );
        ProcessingSettings processingSettings = new ProcessingSettings(
                "Auto",
                6000,
                500,
                SummaryType.TLDR,
                "German",
                true,
                false,
                true,
                false
        );

        String result = ollamaClient.generateSimpleSummary(
                SummaryType.TLDR,
                "Paper content.",
                modelSettings,
                processingSettings
        );

        assertEquals("Generated summary", result);
        JsonNode requestJson = objectMapper.readTree(handler.requests.get(0));
        assertEquals("llama3.1:8b", requestJson.get("model").asText());
        assertTrue(requestJson.get("prompt").asText().contains("Write the summary in German."));
        assertTrue(requestJson.get("prompt").asText().contains("Include important references"));
    }

    private static class FakeOllamaHandler implements HttpHandler {

        private final List<String> requests = new ArrayList<>();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] responseBytes = """
                    {
                      "response": " Generated summary ",
                      "done": true
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }
    }
}
