package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.Paper;

public class PaperViewModel {
    private final Paper paper;

    private final StringProperty title;
    private final StringProperty authors;
//    private final StringProperty abstractText;
// Need to add this thing later!!!

    public PaperViewModel(Paper paper/*, StringProperty abstractText*/) {
        this.paper = paper;
        this.title = new SimpleStringProperty(paper.title());
        this.authors = new SimpleStringProperty(paper.authors());
//        this.abstractText = new SimpleStringProperty(paper.abstractText());
// Need to add this thing later!!!
    }

    public String makePreview(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 250 ? text : text.substring(0, 250) + "...";
    }
    public Paper getPaper() {
        return this.paper;
    }

    public StringProperty titleProperty() {
        return title;
    }
    public StringProperty authorsProperty() {
        return authors;
    }

}
