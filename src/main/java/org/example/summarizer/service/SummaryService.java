package org.example.summarizer.service;

import org.example.summarizer.infrastructure.ollama.OllamaClient;
import org.example.summarizer.infrastructure.pdf.TextExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class SummaryService {
    private final TextExtractor textExtractor;
    private final OllamaClient ollamaClient;

    public SummaryService(TextExtractor textExtractor, OllamaClient ollamaClient) {
        this.textExtractor = textExtractor;
        this.ollamaClient = ollamaClient;
    }

    public boolean isNotTooLong(String content) {
        if (content.length() < 12000) {
            return true;
        }
        return false;
    }

    public String prepareText(String url) throws IOException, InterruptedException {

        Optional<String> content;
        if (url.contains("https")) {
            content = textExtractor.extractFromUrl(url);
        } else {
            content = textExtractor.extractFromPath(Path.of(url));
        }


        return content.orElse("Chosen paper was empty!");
    }

}