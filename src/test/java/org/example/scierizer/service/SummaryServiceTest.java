//package org.example.scierizer.service;
//
//import org.example.scierizer.infrastructure.ollama.OllamaClient;
//import org.example.scierizer.infrastructure.pdf.TextExtractor;
//import org.example.scierizer.infrastructure.pdf.TextShredder;
//import org.example.scierizer.viewmodel.SummaryType;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Timeout;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assumptions.assumeTrue;
//
//@Tag("integration")
//class SummaryServiceIntegrationTest {
//    private static final String PDF_PATH = "C:\\\\Users\\\\marcs\\\\Downloads\\\\2607.01390v1.pdf";
//
//    @Test
//    @Timeout(300)
//    void shouldExtractTextFromPdfAndGenerateRealSummaryWithOllama()
//            throws IOException, InterruptedException {
//
//        assumeTrue(isOllamaRunning(), "Ollama is not running on http://localhost:11434");
//
//        Path pdfPath = Path.of(PDF_PATH);
//
//        assumeTrue(
//                Files.exists(pdfPath),
//                "Replace PDF_PATH with a real absolute path to a PDF file"
//        );
//
//        TextExtractor textExtractor = new TextExtractor();
//        OllamaClient ollamaClient = new OllamaClient();
//        TextShredder textShredder = new TextShredder();
//
//        SummaryService summaryService = new SummaryService(
//                textExtractor,
//                ollamaClient,
//                textShredder
//        );
//
//        String summary = summaryService.getSummary(
//                pdfPath.toString(),
//                SummaryType.STRUCTURED
//        );
//
//        assertNotNull(summary);
//        assertFalse(summary.isBlank());
//
//        System.out.println();
//        System.out.println("===== GENERATED SUMMARY =====");
//        System.out.println(summary);
//        System.out.println("=============================");
//        System.out.println();
//    }
//
//    private boolean isOllamaRunning() {
//        HttpClient httpClient = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofSeconds(2))
//                .build();
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:11434/api/tags"))
//                .timeout(Duration.ofSeconds(2))
//                .GET()
//                .build();
//
//        try {
//            HttpResponse<String> response = httpClient.send(
//                    request,
//                    HttpResponse.BodyHandlers.ofString()
//            );
//
//            return response.statusCode() == 200;
//
//        } catch (IOException e) {
//            return false;
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return false;
//        }
//    }
//}