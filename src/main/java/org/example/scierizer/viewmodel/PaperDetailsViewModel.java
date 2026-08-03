package org.example.scierizer.viewmodel;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.example.scierizer.domain.LocalDateTransformer;
import org.example.scierizer.domain.Paper;
import org.example.scierizer.domain.Summary;
import org.example.scierizer.domain.settings.AppSettings;
import org.example.scierizer.domain.settings.StorageSettings;
import org.example.scierizer.infrastructure.persistence.DBInitializer;
import org.example.scierizer.infrastructure.persistence.SQLitePaperRepository;
import org.example.scierizer.infrastructure.persistence.SQLiteSummaryRepository;
import org.example.scierizer.service.ReportExportService;
import org.example.scierizer.service.SettingsService;
import org.example.scierizer.service.SummaryService;

import java.nio.file.Path;

public class PaperDetailsViewModel {

    private final Paper paper;
    private final SummaryService summaryService;
    private final SettingsService settingsService;
    private final ReportExportService reportExportService;

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty authors = new SimpleStringProperty();
    private final StringProperty published = new SimpleStringProperty();
    private final StringProperty updated = new SimpleStringProperty();
    private final StringProperty source = new SimpleStringProperty();
    private final StringProperty abstractText = new SimpleStringProperty();
    private final DBInitializer dbInitializer;
    private final ObjectProperty<SummaryType> selectedSummaryType = new SimpleObjectProperty<>();

    private final StringProperty generatedSummary = new SimpleStringProperty("");
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public void generateSummary() {
        SummaryType summaryType = selectedSummaryType.get();

        if (summaryType == null) {
            errorMessage.set("Choose summary type");
            return;
        }
        isLoading.set(true);
        errorMessage.set("");
        generatedSummary.set("");

        Task<String> summaryTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return summaryService.getSummary(source.get(), summaryType);
            }
        };
        summaryTask.setOnSucceeded(event -> {
            generatedSummary.set(summaryTask.getValue());
            isLoading.set(false);
        });
        summaryTask.setOnFailed(event -> {
            Throwable execption = summaryTask.getException();
            execption.printStackTrace();
            errorMessage.set("Could not generate summary");
            isLoading.set(false);
        });
        Thread thread = new Thread(summaryTask);
        thread.setDaemon(true);
        thread.start();
    }
    public void savePaper() {
        try {
            SQLitePaperRepository paperRepository = new SQLitePaperRepository(dbInitializer);
            paperRepository.save(paper);

            AppSettings settings = loadSettings();
            StorageSettings storageSettings = settings.storage();
            if (storageSettings.downloadPdfWhenSaving()) {
                reportExportService.downloadPdf(paper, directoryOrDefault(storageSettings.papersDirectory()));
            }
            errorMessage.set("");
        } catch (RuntimeException e) {
            errorMessage.set("Could not save paper");
        }
    }

    public void saveSummary() {
        try {
            SQLiteSummaryRepository summaryRepository = new SQLiteSummaryRepository(dbInitializer);
            Summary summary = new Summary(
                    0,
                    paper.title(),
                    paper.authors(),
                    generatedSummary.get(),
                    paper.datePublished(),
                    paper.pdfLink(),
                    selectedSummaryType.get()
            );
            summaryRepository.save(summary);

            AppSettings settings = loadSettings();
            StorageSettings storageSettings = settings.storage();
            if (storageSettings.exportSummaryWhenSaving()) {
                reportExportService.exportSummary(
                        summary,
                        directoryOrDefault(storageSettings.summariesDirectory()),
                        storageSettings.summaryExportFormat()
                );
            }
            errorMessage.set("");
        } catch (RuntimeException e) {
            errorMessage.set("Could not save summary");
        }
    }


    public PaperDetailsViewModel(Paper paper, SummaryService summaryService, DBInitializer dbInitializer) {
        this(paper, summaryService, dbInitializer, null, new ReportExportService());
    }

    public PaperDetailsViewModel(
            Paper paper,
            SummaryService summaryService,
            DBInitializer dbInitializer,
            SettingsService settingsService,
            ReportExportService reportExportService
    ) {
        this.paper = paper;
        this.summaryService = summaryService;
        this.dbInitializer = dbInitializer;
        this.settingsService = settingsService;
        this.reportExportService = reportExportService;

        this.title.set(paper.title());
        this.authors.set(paper.authors());
        this.published.set(LocalDateTransformer.convertToString(paper.datePublished()));
        this.updated.set(LocalDateTransformer.convertToString(paper.dateUpdated()));
        this.source.set(paper.pdfLink());
        this.abstractText.set(paper.abstractText());
        this.selectedSummaryType.set(summaryService.defaultSummaryType());
        dbInitializer.initialize();
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
        return dbInitializer.getAppDbPath();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public Paper paper() {
        return paper;
    }
    public StringProperty authorsProperty() {
        return authors;
    }

    public StringProperty publishedDateProperty() {
        return published;
    }

    public StringProperty updatedDateProperty() {
        return updated;
    }

    public StringProperty pdfLinkProperty() {
        return source;
    }

    public StringProperty abstractTextProperty() {
        return abstractText;
    }

    public ObjectProperty<SummaryType> selectedSummaryTypeProperty() {
        return selectedSummaryType;
    }

    public StringProperty generatedSummaryProperty() {
        return generatedSummary;
    }

    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
}
