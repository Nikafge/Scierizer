package org.example.summarizer.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Summary(
        long id,
        String title,
        String authors,
        String summary,
        LocalDate publishedAt,
        String pdf_link
) {}