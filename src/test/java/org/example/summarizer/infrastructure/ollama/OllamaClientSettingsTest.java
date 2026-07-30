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
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void generateSimpleSummarySupportsOpenAiCompatibleCloudProvider() throws IOException {
        CloudHandler cloudHandler = new CloudHandler("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": " Cloud summary "
                      }
                    }
                  ]
                }
                """);
        server.createContext("/v1/chat/completions", cloudHandler);
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "OpenAI",
                "gpt-test",
                "http://localhost:11434",
                "Auto",
                "http://localhost:" + server.getAddress().getPort(),
                "test-openai-key",
                true,
                8192,
                777,
                2
        );

        String result = ollamaClient.generateSimpleSummary(
                SummaryType.TLDR,
                "Paper content.",
                modelSettings,
                AppSettings.defaults().processing()
        );

        assertEquals("Cloud summary", result);
        assertEquals("Bearer test-openai-key", cloudHandler.authorizationHeader);
        JsonNode requestJson = objectMapper.readTree(cloudHandler.requests.get(0));
        assertEquals("gpt-test", requestJson.get("model").asText());
        assertEquals(777, requestJson.get("max_completion_tokens").asInt());
        assertEquals("developer", requestJson.get("messages").get(0).get("role").asText());
        assertEquals("user", requestJson.get("messages").get(1).get("role").asText());
    }

    @Test
    void generateSimpleSummarySupportsAnthropicProvider() throws IOException {
        CloudHandler cloudHandler = new CloudHandler("""
                {
                  "content": [
                    {
                      "type": "text",
                      "text": " Anthropic summary "
                    }
                  ]
                }
                """);
        server.createContext("/v1/messages", cloudHandler);
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Anthropic",
                "claude-test",
                "http://localhost:11434",
                "Auto",
                "http://localhost:" + server.getAddress().getPort(),
                "test-anthropic-key",
                true,
                8192,
                888,
                2
        );

        String result = ollamaClient.generateSimpleSummary(
                SummaryType.TLDR,
                "Paper content.",
                modelSettings,
                AppSettings.defaults().processing()
        );

        assertEquals("Anthropic summary", result);
        assertEquals("test-anthropic-key", cloudHandler.apiKeyHeader);
        assertEquals("2023-06-01", cloudHandler.anthropicVersionHeader);
        JsonNode requestJson = objectMapper.readTree(cloudHandler.requests.get(0));
        assertEquals("claude-test", requestJson.get("model").asText());
        assertEquals(888, requestJson.get("max_tokens").asInt());
        assertTrue(requestJson.hasNonNull("system"));
        assertEquals("user", requestJson.get("messages").get(0).get("role").asText());
    }

    @Test
    void extractTextFromImageUsesUnlimitedOcrChatModel() throws IOException {
        ChatHandler chatHandler = new ChatHandler();
        server.createContext("/api/chat", chatHandler);
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Ollama",
                "summary-model",
                "http://localhost:" + server.getAddress().getPort(),
                "Auto",
                "",
                "",
                false,
                8192,
                1024,
                2
        );

        String result = ollamaClient.extractTextFromImage("base64-png", 3, modelSettings);

        assertEquals("OCR text", result);
        JsonNode requestJson = objectMapper.readTree(chatHandler.requests.get(0));
        assertEquals(OllamaClient.UNLIMITED_OCR_MODEL, requestJson.get("model").asText());
        assertEquals("user", requestJson.get("messages").get(0).get("role").asText());
        assertTrue(requestJson.get("messages").get(0).get("content").asText().contains("page 3"));
        assertEquals("base64-png", requestJson.get("messages").get(0).get("images").get(0).asText());
    }

    @Test
    void testConnectionChecksOllamaModelAvailability() {
        server.createContext("/api/tags", new StaticJsonHandler("""
                {
                  "models": [
                    {
                      "name": "qwen3:8b",
                      "model": "qwen3:8b"
                    }
                  ]
                }
                """));
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Ollama",
                "qwen3:8b",
                "http://localhost:" + server.getAddress().getPort(),
                "Auto",
                "",
                "",
                false,
                8192,
                1024,
                2
        );

        OllamaClient.ConnectionTestResult result = ollamaClient.testConnection(
                modelSettings,
                AppSettings.defaults().processing()
        );

        assertTrue(result.successful());
        assertTrue(result.message().contains("Connected to Ollama"));
    }

    @Test
    void testConnectionReportsMissingOllamaModel() {
        server.createContext("/api/tags", new StaticJsonHandler("""
                {
                  "models": []
                }
                """));
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Ollama",
                "missing-model",
                "http://localhost:" + server.getAddress().getPort(),
                "Auto",
                "",
                "",
                false,
                8192,
                1024,
                2
        );

        OllamaClient.ConnectionTestResult result = ollamaClient.testConnection(
                modelSettings,
                AppSettings.defaults().processing()
        );

        assertFalse(result.successful());
        assertTrue(result.message().contains("ollama pull missing-model"));
    }

    @Test
    void testConnectionChecksUnlimitedOcrModelWhenParsingUsesOcr() {
        server.createContext("/api/tags", new StaticJsonHandler("""
                {
                  "models": [
                    {
                      "name": "qwen3:8b",
                      "model": "qwen3:8b"
                    }
                  ]
                }
                """));
        OllamaClient ollamaClient = new OllamaClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .build(),
                objectMapper
        );
        ModelSettings modelSettings = new ModelSettings(
                "Ollama",
                "qwen3:8b",
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
                "Unlimited-OCR",
                "Auto",
                6000,
                500,
                SummaryType.TLDR,
                "English",
                true,
                true,
                false,
                false
        );

        OllamaClient.ConnectionTestResult result = ollamaClient.testConnection(
                modelSettings,
                processingSettings
        );

        assertFalse(result.successful());
        assertTrue(result.message().contains("ollama pull " + OllamaClient.UNLIMITED_OCR_MODEL));
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

    private static class ChatHandler implements HttpHandler {

        private final List<String> requests = new ArrayList<>();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] responseBytes = """
                    {
                      "message": {
                        "role": "assistant",
                        "content": " title [1, 2, 3, 4]OCR text "
                      },
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

    private static class StaticJsonHandler implements HttpHandler {

        private final String responseBody;

        private StaticJsonHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }
    }

    private static class CloudHandler implements HttpHandler {

        private final List<String> requests = new ArrayList<>();
        private final String responseBody;
        private String authorizationHeader;
        private String apiKeyHeader;
        private String anthropicVersionHeader;

        private CloudHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            authorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");
            apiKeyHeader = exchange.getRequestHeaders().getFirst("x-api-key");
            anthropicVersionHeader = exchange.getRequestHeaders().getFirst("anthropic-version");
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }
    }
}
