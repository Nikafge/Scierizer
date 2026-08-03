package org.example.scierizer.domain;

import org.example.scierizer.viewmodel.SummaryType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Summary(
        int id,
        String title,
        String authors,
        String summary,
        LocalDate datePublished,
        String pdfLink,
        SummaryType summaryType
) {
    public Summary withId(Integer id) {
        return new Summary(id, title, authors, summary, datePublished, pdfLink, summaryType);
    }
}