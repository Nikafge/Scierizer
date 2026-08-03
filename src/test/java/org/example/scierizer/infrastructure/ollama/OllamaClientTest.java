//package org.example.scierizer.infrastructure.ollama;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpServer;
//import org.example.scierizer.viewmodel.SummaryType;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//                                                          //IN ORDER FOR TEST TO WORK, FOLLOW INSTRUCTIONS IN OLLAMA CLIENT. UNCOMMENT THE CODE
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.http.HttpClient;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executors;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class OllamaClientTest {
//
//    private HttpServer server;
//    private FakeOllamaHandler handler;
//    private OllamaClient ollamaClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() throws IOException {
//        server = HttpServer.create(new InetSocketAddress(0), 0);
//
//        handler = new FakeOllamaHandler(objectMapper);
//        server.createContext("/api/generate", handler);
//        server.setExecutor(Executors.newSingleThreadExecutor());
//        server.start();
//
//        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/api/generate";
//
//        ollamaClient = new OllamaClient(
//                baseUrl,
//                HttpClient.newBuilder()
//                        .connectTimeout(Duration.ofSeconds(2))
//                        .build(),
//                objectMapper
//        );
//    }
//
//    @AfterEach
//    void tearDown() {
//        server.stop(0);
//    }
//
//    @Test
//    void generateSimpleSummary_shouldSendTldrPromptAndReturnTrimmedResponse() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "  This is a TLDR summary.  ",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateSimpleSummary(
//                SummaryType.TLDR,
//                "This paper studies graph neural networks."
//        );
//
//        assertEquals("This is a TLDR summary.", result);
//
//        assertEquals(1, handler.requests.size());
//
//        JsonNode requestJson = objectMapper.readTree(handler.requests.get(0));
//
//        assertEquals("gemma4:e4b", requestJson.get("model").asText());
//        assertFalse(requestJson.get("stream").asBoolean());
//
//        assertTrue(requestJson.get("system").asText().contains("You are summarizing a scientific paper"));
//        assertTrue(requestJson.get("prompt").asText().contains("Summarize the following paper in 5-10 sentences"));
//        assertTrue(requestJson.get("prompt").asText().contains("This paper studies graph neural networks."));
//        assertFalse(requestJson.get("prompt").asText().contains("{paper_text}"));
//    }
//
//    @Test
//    void generateSimpleSummary_shouldBuildExecutivePrompt() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Executive summary.",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateSimpleSummary(
//                SummaryType.EXECUTIVE,
//                "Paper content."
//        );
//
//        assertEquals("Executive summary.", result);
//
//        JsonNode requestJson = objectMapper.readTree(handler.requests.get(0));
//
//        assertTrue(requestJson.get("prompt").asText().contains("5-10 paragraph summary"));
//        assertTrue(requestJson.get("prompt").asText().contains("technically literate but non-specialist reader"));
//        assertTrue(requestJson.get("prompt").asText().contains("Paper content."));
//    }
//
//    @Test
//    void generateSimpleSummary_shouldBuildStructuredPrompt() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Structured summary.",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateSimpleSummary(
//                SummaryType.STRUCTURED,
//                "Introduction\\nMethods\\nResults"
//        );
//
//        assertEquals("Structured summary.", result);
//
//        JsonNode requestJson = objectMapper.readTree(handler.requests.get(0));
//
//        assertTrue(requestJson.get("prompt").asText().contains("section by section"));
//        assertTrue(requestJson.get("prompt").asText().contains("preserving its original structure"));
//        assertTrue(requestJson.get("prompt").asText().contains("Introduction\\nMethods\\nResults"));
//    }
//
//    @Test
//    void generateSimpleSummary_shouldBuildResearchNotePrompt() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Research note.",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateSimpleSummary(
//                SummaryType.RESEARCH_NOTE,
//                "The paper explicitly states its contribution."
//        );
//
//        assertEquals("Research note.", result);
//
//        JsonNode requestJson = objectMapper.readTree(handler.requests.get(0));
//
//        assertTrue(requestJson.get("prompt").asText().contains("What is the specific contribution of this work?"));
//        assertTrue(requestJson.get("prompt").asText().contains("Not stated in the provided text"));
//        assertTrue(requestJson.get("prompt").asText().contains("The paper explicitly states its contribution."));
//    }
//
//    @Test
//    void generateChunkedSummary_tldrShouldCallMapForEachChunkAndThenReduce() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Map summary 1",
//                  "done": true
//                }
//                """);
//
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Map summary 2",
//                  "done": true
//                }
//                """);
//
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Final TLDR summary",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateChunkedSummary(
//                SummaryType.TLDR,
//                List.of("Chunk one text.", "Chunk two text.")
//        );
//
//        assertEquals("Final TLDR summary", result);
//        assertEquals(3, handler.requests.size());
//
//        JsonNode firstMapRequest = objectMapper.readTree(handler.requests.get(0));
//        JsonNode secondMapRequest = objectMapper.readTree(handler.requests.get(1));
//        JsonNode reduceRequest = objectMapper.readTree(handler.requests.get(2));
//
//        assertTrue(firstMapRequest.get("prompt").asText().contains("part 1 of 2"));
//        assertTrue(firstMapRequest.get("prompt").asText().contains("Chunk one text."));
//
//        assertTrue(secondMapRequest.get("prompt").asText().contains("part 2 of 2"));
//        assertTrue(secondMapRequest.get("prompt").asText().contains("Chunk two text."));
//
//        assertTrue(reduceRequest.get("prompt").asText().contains("Combine them into a single TLDR summary"));
//        assertTrue(reduceRequest.get("prompt").asText().contains("Map summary 1"));
//        assertTrue(reduceRequest.get("prompt").asText().contains("Map summary 2"));
//        assertFalse(reduceRequest.get("prompt").asText().contains("{combined_map_outputs}"));
//    }
//
//    @Test
//    void generateChunkedSummary_structuredShouldReturnJoinedMapSummariesWithoutReduceCall() {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Section summary 1",
//                  "done": true
//                }
//                """);
//
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Section summary 2",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateChunkedSummary(
//                SummaryType.STRUCTURED,
//                List.of("Section 1 text.", "Section 2 text.")
//        );
//
//        assertEquals("Section summary 1\nSection summary 2", result);
//        assertEquals(2, handler.requests.size());
//    }
//
//    @Test
//    void generateChunkedSummary_researchNoteShouldUseMapAndReducePrompts() throws Exception {
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Contribution found in chunk 1",
//                  "done": true
//                }
//                """);
//
//        handler.enqueueResponse(200, """
//                {
//                  "response": "Final research note",
//                  "done": true
//                }
//                """);
//
//        String result = ollamaClient.generateChunkedSummary(
//                SummaryType.RESEARCH_NOTE,
//                List.of("We contribute a new method.")
//        );
//
//        assertEquals("Final research note", result);
//        assertEquals(2, handler.requests.size());
//
//        JsonNode mapRequest = objectMapper.readTree(handler.requests.get(0));
//        JsonNode reduceRequest = objectMapper.readTree(handler.requests.get(1));
//
//        assertTrue(mapRequest.get("prompt").asText().contains("Contribution of this work"));
//        assertTrue(mapRequest.get("prompt").asText().contains("We contribute a new method."));
//
//        assertTrue(reduceRequest.get("prompt").asText().contains("Below are extracted findings for six questions"));
//        assertTrue(reduceRequest.get("prompt").asText().contains("Contribution found in chunk 1"));
//    }
//
//    @Test
//    void generateSimpleSummary_shouldThrowRuntimeExceptionWhenOllamaReturnsErrorStatus() {
//        handler.enqueueResponse(500, """
//                {
//                  "error": "model failed"
//                }
//                """);
//
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> ollamaClient.generateSimpleSummary(SummaryType.TLDR, "Paper text.")
//        );
//
//        assertTrue(exception.getMessage().contains("Ollama returned status 500"));
//    }
//
//    @Test
//    void generateSimpleSummary_shouldThrowRuntimeExceptionWhenResponseJsonIsInvalid() {
//        handler.enqueueResponse(200, "not-json");
//
//        RuntimeException exception = assertThrows(
//                RuntimeException.class,
//                () -> ollamaClient.generateSimpleSummary(SummaryType.TLDR, "Paper text.")
//        );
//
//        assertEquals("Failed to establish connection with Ollama", exception.getMessage());
//    }
//
//    private static class FakeOllamaHandler implements com.sun.net.httpserver.HttpHandler {
//
//        private final ObjectMapper objectMapper;
//        private final List<String> requests = new ArrayList<>();
//        private final List<FakeResponse> responses = new ArrayList<>();
//
//        private FakeOllamaHandler(ObjectMapper objectMapper) {
//            this.objectMapper = objectMapper;
//        }
//
//        private void enqueueResponse(int statusCode, String body) {
//            responses.add(new FakeResponse(statusCode, body));
//        }
//
//        @Override
//        public void handle(HttpExchange exchange) throws IOException {
//            if (!"POST".equals(exchange.getRequestMethod())) {
//                send(exchange, 405, "Method not allowed");
//                return;
//            }
//
//            String requestBody = new String(
//                    exchange.getRequestBody().readAllBytes(),
//                    StandardCharsets.UTF_8
//            );
//
//            requests.add(requestBody);
//
//            FakeResponse response = responses.isEmpty()
//                    ? new FakeResponse(200, """
//                        {
//                          "response": "default fake response",
//                          "done": true
//                        }
//                        """)
//                    : responses.remove(0);
//
//            send(exchange, response.statusCode(), response.body());
//        }
//
//        private void send(HttpExchange exchange, int statusCode, String body) throws IOException {
//            byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
//
//            exchange.getResponseHeaders().add("Content-Type", "application/json");
//            exchange.sendResponseHeaders(statusCode, responseBytes.length);
//
//            try (OutputStream outputStream = exchange.getResponseBody()) {
//                outputStream.write(responseBytes);
//            }
//        }
//    }
//
//    private record FakeResponse(int statusCode, String body) {
//    }
//}