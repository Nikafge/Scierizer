package org.example.summarizer.domain.settings;

import org.example.summarizer.viewmodel.SummaryType;

public record ProcessingSettings(
        String parsingMethod,
        String chunkingMode,
        int chunkSizeTokens,
        int chunkOverlapTokens,
        SummaryType defaultSummaryType,
        String outputLanguage,
        boolean preserveNumbers,
        boolean includeEquations,
        boolean includeReferences,
        boolean includeFigures
) {
}
