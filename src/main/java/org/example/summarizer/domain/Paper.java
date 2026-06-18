package org.example.summarizer.domain;

import java.time.LocalDate;

public record Paper (
    String title,
    long id,
    LocalDate datePublished,
    LocalDate dateUpdated,
    String authors,
    String pdfLink
) {}
