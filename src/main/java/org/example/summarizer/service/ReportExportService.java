package org.example.summarizer.service;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.infrastructure.arxiv.ArxivHttpPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class ReportExportService {

    private final HttpClient httpClient;

    public ReportExportService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public ReportExportService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Path exportSummary(Summary summary, Path outputDirectory, String format) {
        try {
            Files.createDirectories(outputDirectory);
            String extension = "Text".equalsIgnoreCase(format) ? ".txt" : ".md";
            Path outputPath = nextAvailablePath(outputDirectory, summaryFileName(summary), extension);
            Files.writeString(outputPath, renderSummary(summary, format));
            return outputPath;
        } catch (IOException e) {
            throw new RuntimeException("Could not export summary", e);
        }
    }

    public Path downloadPdf(Paper paper, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Path outputPath = nextAvailablePath(outputDirectory, safeFileName(paper.title()), ".pdf");

            if (paper.pdfLink().startsWith("http://") || paper.pdfLink().startsWith("https://")) {
                URI pdfUri = URI.create(paper.pdfLink());
                HttpRequest request = ArxivHttpPolicy.newRequestBuilder(pdfUri)
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();
                HttpResponse<InputStream> response = ArxivHttpPolicy.send(
                        httpClient,
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );
                if (response.statusCode() != 200) {
                    throw new RuntimeException("PDF download returned status " + response.statusCode());
                }

                try (InputStream inputStream = response.body()) {
                    Files.copy(inputStream, outputPath);
                }
                return outputPath;
            }

            Files.copy(Path.of(paper.pdfLink()), outputPath);
            return outputPath;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Could not download PDF", e);
        }
    }

    private String safeFileName(String value) {
        String safeName = value == null ? "summary" : value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safeName.isBlank()) {
            return "summary";
        }
        return safeName.length() > 120 ? safeName.substring(0, 120) : safeName;
    }

    private String summaryFileName(Summary summary) {
        String summaryType = summary.summaryType() == null ? "summary" : summary.summaryType().getDisplayType();
        return safeFileName(summary.title() + " - " + summaryType);
    }

    private String renderSummary(Summary summary, String format) {
        if ("Text".equalsIgnoreCase(format)) {
            return renderTextSummary(summary);
        }
        return renderMarkdownSummary(summary);
    }

    private String renderMarkdownSummary(Summary summary) {
        return """
                # %s

                - Summary type: %s
                - Authors: %s
                - Published: %s
                - Source: %s

                ## Summary

                %s
                """.formatted(
                valueOrBlank(summary.title()),
                summaryType(summary),
                valueOrBlank(summary.authors()),
                formattedDate(summary),
                valueOrBlank(summary.pdfLink()),
                valueOrBlank(summary.summary())
        );
    }

    private String renderTextSummary(Summary summary) {
        return """
                %s

                Summary type: %s
                Authors: %s
                Published: %s
                Source: %s

                Summary

                %s
                """.formatted(
                valueOrBlank(summary.title()),
                summaryType(summary),
                valueOrBlank(summary.authors()),
                formattedDate(summary),
                valueOrBlank(summary.pdfLink()),
                valueOrBlank(summary.summary())
        );
    }

    private String summaryType(Summary summary) {
        return summary.summaryType() == null ? "" : summary.summaryType().getDisplayType();
    }

    private String formattedDate(Summary summary) {
        if (summary.datePublished() == null) {
            return "";
        }
        return summary.datePublished().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
    }

    private Path nextAvailablePath(Path directory, String fileName, String extension) {
        Path candidate = directory.resolve(fileName + extension);
        int suffix = 2;

        while (Files.exists(candidate)) {
            candidate = directory.resolve(fileName + " (" + suffix + ")" + extension);
            suffix++;
        }

        return candidate;
    }
}
