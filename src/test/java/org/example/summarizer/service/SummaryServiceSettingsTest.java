package org.example.summarizer.service;

import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;
import org.example.summarizer.domain.settings.StorageSettings;
import org.example.summarizer.infrastructure.ollama.OllamaClient;
import org.example.summarizer.infrastructure.pdf.TextExtractor;
import org.example.summarizer.infrastructure.pdf.TextShredder;
import org.example.summarizer.viewmodel.SummaryType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryServiceSettingsTest {

    @TempDir
    Path tempDirectory;

    @Test
    void getSummaryUsesManualChunkingAndConfiguredModelSettings() throws Exception {
        SettingsService settingsService = new SettingsService(tempDirectory);
        settingsService.saveSettings(new AppSettings(
                1,
                new ModelSettings("Ollama", "configured-model", "http://localhost:11434", "Auto", "", "", false, 4096, 512, 3),
                new ProcessingSettings("Manual", 2, 1, SummaryType.RESEARCH_NOTE, "English", true, true, false, false),
                new StorageSettings("", "", "", "Markdown", false, false, false, true)
        ));
        FakeOllamaClient ollamaClient = new FakeOllamaClient();
        SummaryService summaryService = new SummaryService(
                new FakeTextExtractor("abcdefghijklmnop"),
                ollamaClient,
                new TextShredder(),
                settingsService
        );

        assertEquals("chunked summary", summaryService.getSummary("paper.pdf", SummaryType.TLDR));
        assertEquals("configured-model", ollamaClient.modelName);
        assertEquals("Manual", ollamaClient.chunkingMode);
        assertEquals(3, ollamaClient.chunkCount);
        assertEquals(SummaryType.RESEARCH_NOTE, summaryService.defaultSummaryType());
    }

    @Test
    void getSummaryKeepsExtractedTextInConfiguredTemporaryDirectory() throws Exception {
        Path temporaryDirectory = tempDirectory.resolve("extracted");
        SettingsService settingsService = new SettingsService(tempDirectory);
        settingsService.saveSettings(new AppSettings(
                1,
                new ModelSettings("Ollama", "configured-model", "http://localhost:11434", "Auto", "", "", false, 4096, 512, 3),
                new ProcessingSettings("Auto", 6000, 500, SummaryType.TLDR, "English", true, true, false, false),
                new StorageSettings("", "", temporaryDirectory.toString(), "Markdown", false, false, true, false)
        ));
        SummaryService summaryService = new SummaryService(
                new FakeTextExtractor("short paper text"),
                new FakeOllamaClient(),
                new TextShredder(),
                settingsService
        );

        assertEquals("simple summary", summaryService.getSummary("paper.pdf", SummaryType.TLDR));
        assertEquals("short paper text", Files.readString(temporaryDirectory.resolve("paper.pdf.txt")));
    }

    private static class FakeTextExtractor extends TextExtractor {
        private final String content;

        private FakeTextExtractor(String content) {
            this.content = content;
        }

        @Override
        public Optional<String> extractFromPath(Path pdfPath) {
            return Optional.of(content);
        }
    }

    private static class FakeOllamaClient extends OllamaClient {
        private String modelName;
        private String chunkingMode;
        private int chunkCount;

        @Override
        public String generateSimpleSummary(
                SummaryType summaryType,
                String content,
                ModelSettings modelSettings,
                ProcessingSettings processingSettings
        ) {
            return "simple summary";
        }

        @Override
        public String generateChunkedSummary(
                SummaryType summaryType,
                List<String> chapters,
                ModelSettings modelSettings,
                ProcessingSettings processingSettings
        ) {
            this.modelName = modelSettings.modelName();
            this.chunkingMode = processingSettings.chunkingMode();
            this.chunkCount = chapters.size();
            return "chunked summary";
        }
    }
}
