package org.example.summarizer.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.service.PaperSearchService;

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

        if (query.isEmpty() || query == null) {
            errorMessage.set("Type something to search");
            papers.clear();
            return;
        }
        if (chosenCategory.isNull().get() || chosenCategory == null) {
            errorMessage.set("Select a category");
            papers.clear();
            return;
        }
        loading.set(true);
        try {

            errorMessage.set("");
            List<Paper> foundPapers = paperSearchService.search(query, chosenCategory.get());
            papers.setAll(foundPapers.stream().map(paper -> new PaperViewModel(paper)).toList());

        } catch (Exception e) {
            errorMessage.set("Something went wrong");
        }
        loading.set(false);

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
