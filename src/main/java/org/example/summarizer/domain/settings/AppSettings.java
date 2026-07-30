package org.example.summarizer.domain.settings;

import org.example.summarizer.viewmodel.SummaryType;

public record AppSettings(
        int version,
        ModelSettings model,
        ProcessingSettings processing,
        StorageSettings storage
) {
    public static AppSettings defaults() {
        return new AppSettings(
                1,
                new ModelSettings(
                        "Ollama",
                        "qwen3:8b",
                        "http://localhost:11434",
                        "Auto",
                        "",
                        "",
                        false,
                        8192,
                        1024,
                        10
                ),
                new ProcessingSettings(
                        "Auto",
                        "Auto",
                        6000,
                        500,
                        SummaryType.STRUCTURED,
                        "English",
                        true,
                        true,
                        false,
                        true
                ),
                new StorageSettings(
                        "",
                        "",
                        "",
                        "Markdown",
                        true,
                        false,
                        false,
                        false
                )
        );
    }
}
