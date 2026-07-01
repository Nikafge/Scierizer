package org.example.summarizer.viewmodel;

public enum Category {
    PHYSICS ("physics"),
    COMPUTER_SCIENCE ("cs"),
    QUANTITATIVE_FINANCE ("q-fin"),
    ENGINEERING ("eess"),
    MATHEMATICS ("math"),
    QUANTITATIVE_BIOLOGY ("q-bio"),
    STATISTICS ("stat"),
    ECONOMICS ("econ");

    private final String displayName;
//    private final String arxivCode;

    Category(String displayName/*, String arxivCode*/) {
        this.displayName = displayName;
//        this.arxivCode = arxivCode;
    }
    public String getDisplayName() {
        return displayName;
    }
//    String getArxivCode() {
//        return arxivCode;
//    }

}
