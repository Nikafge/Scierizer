package org.example.scierizer;

import org.example.scierizer.infrastructure.arxiv.ArxivClient;
import org.example.scierizer.infrastructure.ollama.OllamaClient;
import org.example.scierizer.infrastructure.pdf.TextExtractor;
import org.example.scierizer.infrastructure.pdf.TextShredder;
import org.example.scierizer.infrastructure.persistence.DBInitializer;
import org.example.scierizer.service.PaperSearchService;
import org.example.scierizer.service.ReportExportService;
import org.example.scierizer.service.SettingsService;
import org.example.scierizer.service.SummaryService;
import org.example.scierizer.service.TemporaryFilesCleaner;

import java.nio.file.Path;
import java.sql.SQLException;

/** Creates and owns the application's shared infrastructure and services. */
public class AppContext {
    private final DBInitializer dbInitializer;
    private final PaperSearchService paperSearchService;
    private final SettingsService settingsService;
    private final ReportExportService reportExportService;
    private final SummaryService summaryService;
    private final TemporaryFilesCleaner temporaryFilesCleaner;

    public AppContext() {
        dbInitializer = new DBInitializer(Path.of(System.getProperty("user.home"), ".scierizer"));
        settingsService = new SettingsService(dbInitializer.getAppDbPath());
        reportExportService = new ReportExportService();
        paperSearchService = new PaperSearchService(new ArxivClient());
        summaryService = new SummaryService(
                new TextExtractor(),
                new OllamaClient(),
                new TextShredder(),
                settingsService
        );
        temporaryFilesCleaner = new TemporaryFilesCleaner(settingsService);
    }

    public void initialize() throws SQLException {
        dbInitializer.initialize();
    }

    public DBInitializer dbInitializer() {
        return dbInitializer;
    }

    public PaperSearchService paperSearchService() {
        return paperSearchService;
    }

    public SettingsService settingsService() {
        return settingsService;
    }

    public ReportExportService reportExportService() {
        return reportExportService;
    }

    public SummaryService summaryService() {
        return summaryService;
    }

    public TemporaryFilesCleaner temporaryFilesCleaner() {
        return temporaryFilesCleaner;
    }
}
