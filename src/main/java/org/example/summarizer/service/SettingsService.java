package org.example.summarizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.ModelSettings;
import org.example.summarizer.domain.settings.ProcessingSettings;
import org.example.summarizer.domain.settings.StorageSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsService {

    private static final String SETTINGS_FILE_NAME = "settings.json";

    private final Path settingsFilePath;
    private final ObjectMapper objectMapper;

    public SettingsService(Path appDataDirectory) {
        this.settingsFilePath = appDataDirectory.resolve(SETTINGS_FILE_NAME);
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public AppSettings loadSettings() {
        if (Files.notExists(settingsFilePath)) {
            return AppSettings.defaults();
        }

        try {
            return withDefaults(objectMapper.readValue(settingsFilePath.toFile(), AppSettings.class));
        } catch (IOException e) {
            throw new RuntimeException("Could not load settings from " + settingsFilePath, e);
        }
    }

    public void saveSettings(AppSettings settings) {
        try {
            Files.createDirectories(settingsFilePath.getParent());
            objectMapper.writeValue(settingsFilePath.toFile(), settings);
        } catch (IOException e) {
            throw new RuntimeException("Could not save settings to " + settingsFilePath, e);
        }
    }

    public Path getSettingsFilePath() {
        return settingsFilePath;
    }

    public Path getSettingsDirectory() {
        return settingsFilePath.getParent();
    }

    private AppSettings withDefaults(AppSettings loadedSettings) {
        AppSettings defaults = AppSettings.defaults();
        if (loadedSettings == null) {
            return defaults;
        }

        return new AppSettings(
                loadedSettings.version() == 0 ? defaults.version() : loadedSettings.version(),
                modelWithDefaults(loadedSettings.model(), defaults.model()),
                processingWithDefaults(loadedSettings.processing(), defaults.processing()),
                storageWithDefaults(loadedSettings.storage(), defaults.storage())
        );
    }

    private ModelSettings modelWithDefaults(ModelSettings settings, ModelSettings defaults) {
        if (settings == null) {
            return defaults;
        }

        return new ModelSettings(
                textOrDefault(settings.provider(), defaults.provider()),
                textOrDefault(settings.modelName(), defaults.modelName()),
                textOrDefault(settings.ollamaBaseUrl(), defaults.ollamaBaseUrl()),
                textOrDefault(settings.contextMode(), defaults.contextMode()),
                textOrDefault(settings.cloudEndpoint(), defaults.cloudEndpoint()),
                textOrDefault(settings.apiKeyReference(), defaults.apiKeyReference()),
                settings.rememberApiKey(),
                positiveOrDefault(settings.contextTokens(), defaults.contextTokens()),
                positiveOrDefault(settings.maxOutputTokens(), defaults.maxOutputTokens()),
                positiveOrDefault(settings.requestTimeoutMinutes(), defaults.requestTimeoutMinutes())
        );
    }

    private ProcessingSettings processingWithDefaults(ProcessingSettings settings, ProcessingSettings defaults) {
        if (settings == null) {
            return defaults;
        }

        return new ProcessingSettings(
                textOrDefault(settings.parsingMethod(), defaults.parsingMethod()),
                textOrDefault(settings.chunkingMode(), defaults.chunkingMode()),
                positiveOrDefault(settings.chunkSizeTokens(), defaults.chunkSizeTokens()),
                Math.max(0, settings.chunkOverlapTokens()),
                settings.defaultSummaryType() == null ? defaults.defaultSummaryType() : settings.defaultSummaryType(),
                textOrDefault(settings.outputLanguage(), defaults.outputLanguage()),
                settings.preserveNumbers(),
                settings.includeEquations(),
                settings.includeReferences(),
                settings.includeFigures()
        );
    }

    private StorageSettings storageWithDefaults(StorageSettings settings, StorageSettings defaults) {
        if (settings == null) {
            return defaults;
        }

        return new StorageSettings(
                textOrDefault(settings.papersDirectory(), defaults.papersDirectory()),
                textOrDefault(settings.summariesDirectory(), defaults.summariesDirectory()),
                textOrDefault(settings.temporaryDirectory(), defaults.temporaryDirectory()),
                textOrDefault(settings.summaryExportFormat(), defaults.summaryExportFormat()),
                settings.downloadPdfWhenSaving(),
                settings.exportSummaryWhenSaving(),
                settings.keepExtractedText(),
                settings.clearTemporaryFilesOnExit()
        );
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private int positiveOrDefault(int value, int defaultValue) {
        return value <= 0 ? defaultValue : value;
    }
}
