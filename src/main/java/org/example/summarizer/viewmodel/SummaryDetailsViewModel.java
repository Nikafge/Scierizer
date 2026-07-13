package org.example.summarizer.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.summarizer.domain.LocalDateTransformer;
import org.example.summarizer.domain.Summary;

public class SummaryDetailsViewModel {
    private final StringProperty paperTitleProperty;
    private final StringProperty summaryProperty;
    private final StringProperty summaryTypeProperty;
    private final StringProperty sourcePdfLinkProperty;
    private final StringProperty datePublishedProperty;

    public SummaryDetailsViewModel (Summary summary) {
        paperTitleProperty = new SimpleStringProperty(summary.title());
        this.summaryProperty = new SimpleStringProperty(summary.summary());
        summaryTypeProperty = new SimpleStringProperty(summary.summaryType().getDisplayType());
        sourcePdfLinkProperty = new SimpleStringProperty(summary.pdfLink());
        datePublishedProperty = new SimpleStringProperty(LocalDateTransformer.convertToString(summary.datePublished()));
    }

    public StringProperty PaperTitle() {
        return paperTitleProperty;
    }
    public StringProperty summary() {
        return summaryProperty;
    }
    public StringProperty summaryType() {
        return summaryTypeProperty;
    }
    public StringProperty sourcePdfLink() {
        return sourcePdfLinkProperty;
    }
    public StringProperty datePublished() {
        return datePublishedProperty;
    }
}
