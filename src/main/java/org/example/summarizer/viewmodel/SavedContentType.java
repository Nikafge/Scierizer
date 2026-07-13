package org.example.summarizer.viewmodel;

public enum SavedContentType {
    ARTICLES ("articles"),
    SUMMARIES ("summaries");

    private final String displayType;

    SavedContentType(String displayType) {
        this.displayType = displayType;
    }
    public String getDisplayType() {
        return displayType;
    }

}
