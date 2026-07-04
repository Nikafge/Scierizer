package org.example.summarizer.viewmodel;

public enum SummaryType {
    TLDR ("tldr"),
    EXECUTIVE ("executive"),
    STRUCTURED ("structured"),
    RESEARCH_NOTE ("research_note"),
    TEACHING_MODE ("teaching_mode");

    private final String displayType;

    SummaryType(String displayType) {
        this.displayType = displayType;
    }
    public String getDisplayType() {
        return displayType;
    }
}
