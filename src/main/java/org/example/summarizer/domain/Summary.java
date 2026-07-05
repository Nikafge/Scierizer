package org.example.summarizer.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Summary(
        int id,
        String title,
        String authors,
        String summary,
        LocalDate date_published,
        String pdf_link
) {}