package org.example.summarizer.viewmodel;

public enum Category {
    Physics ("PHYSICS"),
    Computer_Science ("COMP_SCI"),
    Quantitative_Finance ("QUANT_FINANCE"),
    Engineering ("ENGINEERING"),
    Mathematics ("MATHEMATICS"),
    Quantitative_Biology ("QUANT_BIOLOGY"),
    Statistics ("STATISTICS"),
    Economics ("ECONOMICS");

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
