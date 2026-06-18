package org.example.summarizer.viewmodel;

public enum Category {
    Physics ("Physics"),
    Computer_Science ("Comp-sci"),
    Quantitative_Finance ("Quant. finance"),
    Engineering ("Engineering"),
    Mathematics ("Mathematics"),
    Quantitative_biology ("Quant. biology"),
    Statistics ("Statistics"),
    Economics ("Economics");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

}
