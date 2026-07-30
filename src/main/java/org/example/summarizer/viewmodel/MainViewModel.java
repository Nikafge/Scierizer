package org.example.summarizer.viewmodel;

import javafx.beans.property.*;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.service.PaperSearchService;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class MainViewModel {
    private static final Duration LOAD_MORE_COOLDOWN = Duration.seconds(3);

    private final PaperSearchService paperSearchService;

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<Category> chosenCategory = new SimpleObjectProperty<>();
    private final ObservableList<PaperViewModel> papers = FXCollections.observableArrayList();

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty loadMoreVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty loadMoreAvailable = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private int nextResultStart = 0;
    private String activeSearchQuery = "";
    private Category activeCategory;
    private int searchGeneration = 0;


    public MainViewModel(PaperSearchService paperSearchService) {
        this.paperSearchService = paperSearchService;
        searchQuery.addListener((observable, oldValue, newValue) -> invalidateLoadMore());
        chosenCategory.addListener((observable, oldValue, newValue) -> invalidateLoadMore());
    }

    public void search() {
        String query = searchQuery.get();

        if (query == null || query.isEmpty()) {
            errorMessage.set("Type something to search");
            papers.clear();
            invalidateLoadMore();
            return;
        }
        if (chosenCategory.isNull().get()) {
            errorMessage.set("Select a category");
            papers.clear();
            invalidateLoadMore();
            return;
        }

        // new logic for async search using task
        loading.set(true);
        loadMoreVisible.set(false);
        loadMoreAvailable.set(false);
        errorMessage.set("");
        int generation = ++searchGeneration;
        Category category = chosenCategory.get();

        Task<List<Paper>> task = new Task<>() {
            @Override
            protected List<Paper> call() throws IOException, InterruptedException {
                return paperSearchService.search(query, category, 0, PaperSearchService.DEFAULT_PAGE_SIZE);
            }
        };

        task.setOnSucceeded(event -> {
            List<Paper> results = task.getValue();
            papers.setAll(results.stream().map(PaperViewModel::new).toList());
            activeSearchQuery = query;
            activeCategory = category;
            nextResultStart = results.size();
            scheduleLoadMoreAvailability(generation, results.size());
            loading.set(false);
        });

        task.setOnFailed(event -> {
            errorMessage.set("Could not load papers from arXiv");
            loadMoreVisible.set(false);
            loadMoreAvailable.set(false);
            loading.set(false);
        });
        Thread thread = new Thread(task, "paper-search");
        thread.setDaemon(true);
        thread.start();


    }

    public void loadMore() {
        if (loading.get() || !loadMoreAvailable.get() || activeSearchQuery == null || activeSearchQuery.isBlank()) {
            return;
        }

        loading.set(true);
        loadMoreAvailable.set(false);
        errorMessage.set("");
        int generation = ++searchGeneration;
        int start = nextResultStart;
        String query = activeSearchQuery;
        Category category = activeCategory;

        Task<List<Paper>> task = new Task<>() {
            @Override
            protected List<Paper> call() throws IOException, InterruptedException {
                return paperSearchService.search(query, category, start, PaperSearchService.DEFAULT_PAGE_SIZE);
            }
        };

        task.setOnSucceeded(event -> {
            List<Paper> results = task.getValue();
            papers.addAll(results.stream().map(PaperViewModel::new).toList());
            nextResultStart += results.size();
            scheduleLoadMoreAvailability(generation, results.size());
            loading.set(false);
        });

        task.setOnFailed(event -> {
            errorMessage.set("Could not load more papers from arXiv");
            loadMoreAvailable.set(true);
            loading.set(false);
        });

        Thread thread = new Thread(task, "paper-search-more");
        thread.setDaemon(true);
        thread.start();
    }

    public Paper createLocalPdfPaper(Path pdfPath) {
        Objects.requireNonNull(pdfPath, "pdfPath");
        String fileName = pdfPath.getFileName() == null ? "" : pdfPath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Selected file must be a PDF");
        }

        LocalDate importDate = LocalDate.now();
        Path absolutePath = pdfPath.toAbsolutePath().normalize();
        return new Paper(
                localPdfTitle(fileName),
                0,
                "No abstract is available for this uploaded PDF.",
                importDate,
                importDate,
                "Local PDF",
                absolutePath.toString()
        );
    }

    public StringProperty searchQueryProperty () {
        return searchQuery;
    }
    public ObjectProperty<Category> chosenCategoryProperty() {
        return chosenCategory;
    }
    public ObservableList<PaperViewModel> papers() {
        return papers;
    }
    public BooleanProperty loading() {
        return loading;
    }
    public BooleanProperty loadMoreVisible() {
        return loadMoreVisible;
    }
    public BooleanProperty loadMoreAvailable() {
        return loadMoreAvailable;
    }
    public StringProperty errorMessage() {
        return errorMessage;
    }

    private void scheduleLoadMoreAvailability(int generation, int resultCount) {
        boolean hasPotentiallyMoreResults = resultCount >= PaperSearchService.DEFAULT_PAGE_SIZE;
        loadMoreVisible.set(hasPotentiallyMoreResults);
        loadMoreAvailable.set(false);

        if (!hasPotentiallyMoreResults) {
            if (resultCount == 0) {
                errorMessage.set("No more papers found");
            }
            return;
        }

        PauseTransition cooldown = new PauseTransition(LOAD_MORE_COOLDOWN);
        cooldown.setOnFinished(event -> {
            if (generation == searchGeneration && loadMoreVisible.get() && !loading.get()) {
                loadMoreAvailable.set(true);
            }
        });
        cooldown.play();
    }

    private void invalidateLoadMore() {
        searchGeneration++;
        loadMoreVisible.set(false);
        loadMoreAvailable.set(false);
    }

    private String localPdfTitle(String fileName) {
        String title = fileName.replaceFirst("(?i)\\.pdf$", "").trim();
        if (title.isBlank()) {
            return "Uploaded PDF";
        }
        return title;
    }

}
