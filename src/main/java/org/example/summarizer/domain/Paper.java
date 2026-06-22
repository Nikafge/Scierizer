package org.example.summarizer.domain;

import java.time.LocalDate;

public record Paper (
        String title,
        long id,
        String summary,
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
}


