package org.example.summarizer.viewmodel;

public enum Category {
    Physics ("Physics"),
    Computer_Science ("Comp-sci"),
    Quantitative_Finance ("Quant. finance"),
    Engineering ("Engineering"),
    Mathematics ("Mathematics"),
    Quantitative_Biology ("Quant. biology"),
    Statistics ("Statistics"),
    Economics ("Economics");

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
