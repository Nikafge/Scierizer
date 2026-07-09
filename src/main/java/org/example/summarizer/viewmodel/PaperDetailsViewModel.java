package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.Paper;

public class PaperDetailsViewModel {

    private final Paper paper;

    private final StringProperty title = new SimpleStringProperty();

    public PaperDetailsViewModel(Paper paper) {
        this.paper = paper;
        this.title.set(paper.title());
    }

    public StringProperty titleProperty() {
        return title;
    }

    public Paper paper() {
        return paper;
    }
}