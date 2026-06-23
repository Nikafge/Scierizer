package org.example.summarizer.domain;

import java.time.LocalDateTime;

public record Summary(
        long id,
        String paperId,
        String content,
        LocalDateTime generatedAt
) {}