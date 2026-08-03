package org.example.summarizer.infrastructure.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.infrastructure.arxiv.ArxivHttpPolicy;
import org.example.summarizer.infrastructure.ollama.OllamaClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class TextExtractor {
    private static final float OCR_RENDER_DPI = 144;

    // For self-added papers
    public Optional<String> extractFromPath(Path pdfPath) throws IOException {
        Objects.requireNonNull(pdfPath, "pdfPath");
        try (InputStream inputStream = Files.newInputStream(pdfPath)) {
            return extractFromStream(inputStream);
        }
    }

    public Optional<String> extractFromPathWithUnlimitedOcr(
            Path pdfPath,
            OllamaClient ollamaClient,
            ModelSettings modelSettings
    ) throws IOException {
        Objects.requireNonNull(pdfPath, "pdfPath");
        try (InputStream inputStream = Files.newInputStream(pdfPath)) {
            return extractFromStreamWithUnlimitedOcr(inputStream, ollamaClient, modelSettings);
        }
    }

    // For PDF's from arXiv
    public Optional<String> extractFromUrl(String pdfUrl) throws IOException, InterruptedException {

        Objects.requireNonNull(pdfUrl, "pdfUrl");
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest httpRequest = ArxivHttpPolicy.newRequestBuilder(URI.create(pdfUrl)).GET().build();
        HttpResponse<InputStream> response = ArxivHttpPolicy.send(
                httpClient,
                httpRequest,
                HttpResponse.BodyHandlers.ofInputStream()
        );
        try (InputStream inputStream = response.body()) {
            return extractFromStream(inputStream);
        }

    }

    public Optional<String> extractFromUrlWithUnlimitedOcr(
            String pdfUrl,
            OllamaClient ollamaClient,
            ModelSettings modelSettings
    ) throws IOException, InterruptedException {
        Objects.requireNonNull(pdfUrl, "pdfUrl");
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest httpRequest = ArxivHttpPolicy.newRequestBuilder(URI.create(pdfUrl)).GET().build();
        HttpResponse<InputStream> response = ArxivHttpPolicy.send(
                httpClient,
                httpRequest,
                HttpResponse.BodyHandlers.ofInputStream()
        );
        try (InputStream inputStream = response.body()) {
            return extractFromStreamWithUnlimitedOcr(inputStream, ollamaClient, modelSettings);
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

    public Optional<String> extractFromStreamWithUnlimitedOcr(
            InputStream inputStream,
            OllamaClient ollamaClient,
            ModelSettings modelSettings
    ) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(ollamaClient, "ollamaClient");
        Objects.requireNonNull(modelSettings, "modelSettings");

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder text = new StringBuilder();

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, OCR_RENDER_DPI, ImageType.RGB);
                String pageText = ollamaClient.extractTextFromImage(
                        toBase64Jpeg(pageImage),
                        pageIndex + 1,
                        modelSettings
                );

                if (!pageText.isBlank()) {
                    if (text.length() > 0) {
                        text.append("\n\n");
                    }
                    text.append(pageText);
                }
            }

            String result = text.toString().trim();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }

    private String toBase64Jpeg(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(image, "jpg", outputStream);
            if (!written) {
                throw new IOException("Could not encode rendered PDF page as JPEG");
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }
}
