package org.example.scierizer.domain.settings;

public record StorageSettings(
        String papersDirectory,
        String summariesDirectory,
        String temporaryDirectory,
        String summaryExportFormat,
        boolean downloadPdfWhenSaving,
        boolean exportSummaryWhenSaving,
        boolean keepExtractedText,
        boolean clearTemporaryFilesOnExit
) {
}
