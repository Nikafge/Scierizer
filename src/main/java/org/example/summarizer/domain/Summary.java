package org.example.summarizer.domain;

import org.example.summarizer.viewmodel.SummaryType;

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