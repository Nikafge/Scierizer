package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.domain.settings.AppSettings;
import org.example.summarizer.domain.settings.StorageSettings;
import org.example.summarizer.service.ReportExportService;
import org.example.summarizer.service.SettingsService;

import java.nio.file.Path;

public class SummaryDetailsViewModel {
    private final Summary summary;
    private final SettingsService settingsService;
    private final ReportExportService reportExportService;

    private final StringProperty paperTitleProperty;
    private final StringProperty summaryProperty;
    private final StringProperty summaryTypeProperty;
    private final StringProperty sourcePdfLinkProperty;
    private final StringProperty datePublishedProperty;
    private final StringProperty exportStatusProperty;

    public SummaryDetailsViewModel (Summary summary) {
        this(summary, null, new ReportExportService());
    }

    public SummaryDetailsViewModel(
            Summary summary,
            SettingsService settingsService,
            ReportExportService reportExportService
    ) {
        this.summary = summary;
        this.settingsService = settingsService;
        this.reportExportService = reportExportService;
        paperTitleProperty = new SimpleStringProperty(summary.title());
        this.summaryProperty = new SimpleStringProperty(summary.summary());
        summaryTypeProperty = new SimpleStringProperty(summary.summaryType().getDisplayType());
        sourcePdfLinkProperty = new SimpleStringProperty(summary.pdfLink());
        datePublishedProperty = new SimpleStringProperty(LocalDateTransformer.convertToString(summary.datePublished()));
        exportStatusProperty = new SimpleStringProperty("");
    }

    public void exportSummary() {
        try {
            AppSettings settings = loadSettings();
            StorageSettings storageSettings = settings.storage();
            Path outputPath = reportExportService.exportSummary(
                    summary,
                    directoryOrDefault(storageSettings.summariesDirectory()),
                    storageSettings.summaryExportFormat()
            );
            exportStatusProperty.set("Exported to " + outputPath);
        } catch (RuntimeException e) {
            exportStatusProperty.set("Could not export summary");
        }
    }

    private AppSettings loadSettings() {
        if (settingsService == null) {
            return AppSettings.defaults();
        }
        return settingsService.loadSettings();
    }

    private Path directoryOrDefault(String configuredDirectory) {
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            return Path.of(configuredDirectory);
        }
        if (settingsService != null) {
            return settingsService.getSettingsDirectory();
        }
        return Path.of(System.getProperty("user.home"), ".summarizer");
    }

    public StringProperty PaperTitle() {
        return paperTitleProperty;
    }
    public StringProperty summary() {
        return summaryProperty;
    }
    public StringProperty summaryType() {
        return summaryTypeProperty;
    }
    public StringProperty sourcePdfLink() {
        return sourcePdfLinkProperty;
    }
    public StringProperty datePublished() {
        return datePublishedProperty;
    }

    public StringProperty exportStatus() {
        return exportStatusProperty;
    }
}
