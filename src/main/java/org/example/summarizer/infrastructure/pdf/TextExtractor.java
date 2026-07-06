package org.example.summarizer.infrastructure.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class TextExtractor {

    // For self-added papers
    public Optional<String> extractFromPath(Path pdfPath) throws IOException {
        Objects.requireNonNull(pdfPath, "pdfPath");
        try (InputStream inputStream = Files.newInputStream(pdfPath)) {
            return extractFromStream(inputStream);
        }
    }

    // For PDF's from arXiv
    public Optional<String> extractFromUrl(String pdfUrl) throws IOException, InterruptedException {

        Objects.requireNonNull(pdfUrl, "pdfUrl");
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(pdfUrl)).timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream inputStream = response.body()) {
            return extractFromStream(inputStream);
        }

    }

    // To extract data from both above
    public Optional<String> extractFromStream(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            String result = textStripper.getText(document).trim();

            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }
}
