package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.Summary;

public class SummaryViewModel implements SavedListItem {

    private final Summary summary;

    private final StringProperty paperTitle;
    private final StringProperty summaryType;

    public SummaryViewModel(Summary summary) {
        this.summary = summary;
        this.paperTitle = new SimpleStringProperty(summary.title());
        this.summaryType = new SimpleStringProperty(summary.summaryType().getDisplayType());
    }

    public String displayTitle() {
        return paperTitle.get();
    }

    public String displaySubtitle() {
        return summaryType.get();
    }

}
