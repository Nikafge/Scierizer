package org.example.scierizer.domain;

import java.time.LocalDate;

public record Paper (
        String title,
        int id,
        String abstractText,
        LocalDate datePublished,
        LocalDate dateUpdated,
        String authors,
        String pdfLink
) {
    public Paper {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (pdfLink == null) {
            throw new IllegalArgumentException("Link cannot be null");
        }
    }
    public Paper withId(Integer id) {
        return new Paper(title, id, abstractText, datePublished, dateUpdated, authors, pdfLink);
    }
}