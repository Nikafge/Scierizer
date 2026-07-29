package org.example.summarizer.service;

import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;
import org.example.summarizer.domain.settings.StorageSettings;
import org.example.summarizer.infrastructure.ollama.OllamaClient;
import org.example.summarizer.infrastructure.pdf.TextExtractor;
import org.example.summarizer.infrastructure.pdf.TextShredder;
import org.example.summarizer.viewmodel.SummaryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class SummaryService {
    private static final int APPROXIMATE_CHARS_PER_TOKEN = 4;

    private final TextExtractor textExtractor;
    private final OllamaClient ollamaClient;
    private final TextShredder textShredder;
    private final SettingsService settingsService;

    public SummaryService(TextExtractor textExtractor, OllamaClient ollamaClient, TextShredder textShredder) {
        this(textExtractor, ollamaClient, textShredder, null);
    }

    public SummaryService(
            TextExtractor textExtractor,
            OllamaClient ollamaClient,
            TextShredder textShredder,
            SettingsService settingsService
    ) {
        this.textExtractor = textExtractor;
        this.ollamaClient = ollamaClient;
        this.textShredder = textShredder;
        this.settingsService = settingsService;
    }

    private boolean isNotTooLong(String content, ModelSettings modelSettings) {
        int inputBudgetTokens = Math.max(1, modelSettings.contextTokens() - modelSettings.maxOutputTokens());
        return content.length() <= inputBudgetTokens * APPROXIMATE_CHARS_PER_TOKEN;
    }

    public String getSummary(String url, SummaryType summaryType) throws IOException, InterruptedException {
        AppSettings settings = loadSettings();
        ModelSettings modelSettings = settings.model();
        ProcessingSettings processingSettings = settings.processing();

        Optional<String> content;
        if (url.contains("http://") || url.contains("https://")) {
            content = textExtractor.extractFromUrl(url);
        } else {
            content = textExtractor.extractFromPath(Path.of(url));
        }
        if (content.isEmpty())
            throw new IllegalArgumentException("No text was extracted from paper");

        if (settings.storage().keepExtractedText()) {
            saveExtractedText(url, content.get(), settings.storage());
        }

        if (shouldUseSimpleSummary(content.get(), modelSettings, processingSettings)) {
            return ollamaClient.generateSimpleSummary(summaryType, content.get(), modelSettings, processingSettings);
        }
        List<String> paperChunks = chunkContent(content.get(), processingSettings);
        return ollamaClient.generateChunkedSummary(summaryType, paperChunks, modelSettings, processingSettings);
    }

    public SummaryType defaultSummaryType() {
        return loadSettings().processing().defaultSummaryType();
    }

    private AppSettings loadSettings() {
        if (settingsService == null) {
            return AppSettings.defaults();
        }
        return settingsService.loadSettings();
    }

    private boolean shouldUseSimpleSummary(
            String content,
            ModelSettings modelSettings,
            ProcessingSettings processingSettings
    ) {
        return !"Manual".equalsIgnoreCase(processingSettings.chunkingMode())
                && isNotTooLong(content, modelSettings);
    }

    private List<String> chunkContent(String content, ProcessingSettings processingSettings) {
        if ("Manual".equalsIgnoreCase(processingSettings.chunkingMode())) {
            return textShredder.cutPaper(
                    content,
                    processingSettings.chunkSizeTokens(),
                    processingSettings.chunkOverlapTokens()
            );
        }

        List<String> sectionChunks = textShredder.cutPaper(content);
        if (!sectionChunks.isEmpty()) {
            return sectionChunks;
        }

        return textShredder.cutPaper(
                content,
                processingSettings.chunkSizeTokens(),
                processingSettings.chunkOverlapTokens()
        );
    }

    private void saveExtractedText(String source, String content, StorageSettings storageSettings) {
        try {
            Path defaultTemporaryDirectory = settingsService == null
                    ? Path.of(System.getProperty("java.io.tmpdir"), "summarizer")
                    : settingsService.getSettingsDirectory().resolve("tmp");
            Path temporaryDirectory = storageSettings.temporaryDirectory() == null
                    || storageSettings.temporaryDirectory().isBlank()
                    ? defaultTemporaryDirectory
                    : Path.of(storageSettings.temporaryDirectory());
            Files.createDirectories(temporaryDirectory);
            Files.writeString(temporaryDirectory.resolve(extractedTextFileName(source)), content);
        } catch (IOException e) {
            throw new RuntimeException("Could not save extracted text", e);
        }
    }

    private String extractedTextFileName(String source) {
        String normalizedSource = source.replace("\\", "/");
        int lastSeparatorIndex = normalizedSource.lastIndexOf('/');
        String sourceName = lastSeparatorIndex == -1
                ? normalizedSource
                : normalizedSource.substring(lastSeparatorIndex + 1);
        String safeName = sourceName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safeName.isBlank()) {
            safeName = "paper";
        }
        return safeName + ".txt";
    }

}
