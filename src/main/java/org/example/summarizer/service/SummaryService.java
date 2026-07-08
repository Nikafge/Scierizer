package org.example.summarizer.service;

import org.example.summarizer.infrastructure.ollama.OllamaClient;
import org.example.summarizer.infrastructure.pdf.TextExtractor;
import org.example.summarizer.infrastructure.pdf.TextShredder;
import org.example.summarizer.viewmodel.SummaryType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class SummaryService {
    private final TextExtractor textExtractor;
    private final OllamaClient ollamaClient;
    private final TextShredder textShredder;

    public SummaryService(TextExtractor textExtractor, OllamaClient ollamaClient, TextShredder textShredder) {
        this.textExtractor = textExtractor;
        this.ollamaClient = ollamaClient;
        this.textShredder = textShredder;
    }

    private boolean isNotTooLong(String content) {
        return content.length() < 12000;
    }

    public String getSummary(String url, SummaryType summaryType) throws IOException, InterruptedException {

        Optional<String> content;
        if (url.contains("http://") || url.contains("https://")) {
            content = textExtractor.extractFromUrl(url);
        } else {
            content = textExtractor.extractFromPath(Path.of(url));
        }
        if (content.isEmpty())
            throw new IllegalArgumentException("No text was extracted from paper");


        if (isNotTooLong(content.get())) {
            return ollamaClient.generateSimpleSummary(summaryType, content.get());
        }
        List<String> paperChunks = textShredder.cutPaper(content.get());
        return ollamaClient.generateChunkedSummary(summaryType, paperChunks);
    }

}