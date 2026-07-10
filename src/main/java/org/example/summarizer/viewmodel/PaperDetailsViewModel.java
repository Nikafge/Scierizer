package org.example.summarizer.viewmodel;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.infrastructure.persistence.DBInitializer;
import org.example.summarizer.infrastructure.persistence.SQLitePaperRepository;
import org.example.summarizer.infrastructure.persistence.SQLiteSummaryRepository;
import org.example.summarizer.service.SummaryService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PaperDetailsViewModel {

    private final Paper paper;
    private final SummaryService summaryService;

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
        SQLitePaperRepository paperRepository = new SQLitePaperRepository(dbInitializer);
        paperRepository.save(paper);
    }

    public void saveSummary() {
        SQLiteSummaryRepository summaryRepository = new SQLiteSummaryRepository(dbInitializer);
        summaryRepository.save(new Summary(0, paper.title(), paper.authors(), generatedSummary.get(), paper.datePublished(), paper.pdfLink(), selectedSummaryType.get()));
    }


    public PaperDetailsViewModel(Paper paper, SummaryService summaryService, DBInitializer dbInitializer) {
        this.paper = paper;
        this.summaryService = summaryService;
        this.dbInitializer = dbInitializer;

        this.title.set(paper.title());
        this.authors.set(paper.authors());
        this.published.set(LocalDateTransformer.convertToString(paper.datePublished()));
        this.updated.set(LocalDateTransformer.convertToString(paper.dateUpdated()));
        this.source.set(paper.pdfLink());
        this.abstractText.set(paper.abstractText());
        dbInitializer.initialize();
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