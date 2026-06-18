package org.example.summarizer.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.summarizer.domain.Paper;
import org.example.summarizer.service.PaperSearchService;

import java.util.List;

public class MainVeiwModel {
//    private final String searchBar;
//    private final String category;
//
//    public MainVeiwModel(String category, String searchBar) {
//        this.category = category;
//        this.searchBar = searchBar;
//    }

    private final PaperSearchService paperSearchService;

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<Category> chosenCategory = new SimpleObjectProperty<>();
    private final ObservableList<PaperViewModel> papers = FXCollections.observableArrayList();

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");


    public MainVeiwModel(PaperSearchService paperSearchService) {
        this.paperSearchService = paperSearchService;
    }

    public void search() {
        String query = searchQuery.get();

        if (query == null) {
            errorMessage.set("Chose a category!");
        }

        loading.set(true);

        try {
            //Fix this((((
            //            List<Paper> foundPapers = paperSearchService.search(query, chosenCategory);

        } catch (Exception e) {
            errorMessage.set("Something went wrong");
        }
        loading.set(false);

    }

    public StringProperty searchQueryProperty () {
        return searchQuery;
    }
    public ObjectProperty<Category> ChosenCategory() {
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
