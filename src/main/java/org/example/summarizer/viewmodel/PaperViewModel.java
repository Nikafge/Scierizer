package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Paper;

//Commented text shall be used later to widen the information on papers got on saved papers page

public class PaperViewModel implements SavedListItem {
    private final Paper paper;

    private final StringProperty title;
    private final StringProperty authors;

    public PaperViewModel(Paper paper) {
        this.paper = paper;
        this.title = new SimpleStringProperty(paper.title());
        this.authors = new SimpleStringProperty(paper.authors());
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

    public String displayTitle() {
        return titleProperty().get();
    }

    public String displaySubtitle() {
        return authorsProperty().get();
    }


}
