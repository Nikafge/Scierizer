package org.example.summarizer.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.service.PaperSearchService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewModel {

    private final PaperSearchService paperSearchService;

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<Category> chosenCategory = new SimpleObjectProperty<>();
    private final ObservableList<PaperViewModel> papers = FXCollections.observableArrayList();

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");


    public MainViewModel(PaperSearchService paperSearchService) {
        this.paperSearchService = paperSearchService;
    }

    public void search() {
        String query = searchQuery.get();

        if (query == null || query.isEmpty()) {
            errorMessage.set("Type something to search");
            papers.clear();
            return;
        }
        if (chosenCategory.isNull().get()) {
            errorMessage.set("Select a category");
            papers.clear();
            return;
        }

        // new logic for async search using task
        loading.set(true);
        errorMessage.set("");

        Task<List<Paper>> task = new Task<>() {
            @Override
            protected List<Paper> call() throws IOException, InterruptedException {
                return paperSearchService.search(query, chosenCategory.get());
            }
        };

        task.setOnSucceeded(event -> {
            papers.setAll(task.getValue().stream().map(PaperViewModel::new).toList());
            loading.set(false);
        });

        task.setOnFailed(event -> {
            errorMessage.set("Could not load papers from arXiv");
            loading.set(false);
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();


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
    public StringProperty errorMessage() {
        return errorMessage;
    }

}
