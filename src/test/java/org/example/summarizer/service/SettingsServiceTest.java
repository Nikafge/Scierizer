//package org.example.summarizer.service;
//
//import org.example.summarizer.domain.settings.AppSettings;
//import org.example.summarizer.domain.settings.ModelSettings;
//import org.example.summarizer.domain.settings.ProcessingSettings;
//import org.example.summarizer.domain.settings.StorageSettings;
//import org.example.summarizer.viewmodel.SummaryType;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class SettingsServiceTest {
//
//    @TempDir
//    Path tempDirectory;
//
//    @Test
//    void loadSettingsReturnsDefaultsWhenFileDoesNotExist() {
//        SettingsService settingsService = new SettingsService(tempDirectory);
//
//        assertEquals(AppSettings.defaults(), settingsService.loadSettings());
//    }
//
//    @Test
//    void saveSettingsWritesJsonInConfiguredDirectory() throws IOException {
//        SettingsService settingsService = new SettingsService(tempDirectory);
//        AppSettings settings = new AppSettings(
//                1,
//                new ModelSettings(
//                        "Ollama",
//                        "llama3.1:8b",
//                        "http://localhost:11434",
//                        "Manual",
//                        "",
//                        "",
//                        false,
//                        16384,
//                        2048,
//                        15
//                ),
//                new ProcessingSettings(
//                        "Unlimited-OCR",
//                        "Manual",
//                        8000,
//                        750,
//                        SummaryType.TLDR,
//                        "German",
//                        true,
//                        false,
//                        true,
//                        false
//                ),
//                new StorageSettings(
//                        "papers",
//                        "summaries",
//                        "temp",
//                        "Markdown",
//                        true,
//                        true,
//                        false,
//                        true
//                )
//        );
//
//        settingsService.saveSettings(settings);
//
//        Path settingsFilePath = tempDirectory.resolve("settings.json");
//        assertTrue(Files.exists(settingsFilePath));
//        assertTrue(Files.readString(settingsFilePath).contains("\"modelName\" : \"llama3.1:8b\""));
//        assertTrue(Files.readString(settingsFilePath).contains("\"parsingMethod\" : \"Unlimited-OCR\""));
//        assertEquals(settings, settingsService.loadSettings());
//    }
//}
