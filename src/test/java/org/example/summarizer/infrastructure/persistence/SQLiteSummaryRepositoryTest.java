package org.example.summarizer.infrastructure.persistence;

import org.example.summarizer.domain.Paper;
import org.example.summarizer.domain.Summary;
import org.example.summarizer.viewmodel.SummaryType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteSummaryRepositoryTest {
    @TempDir
    Path tempRoot;

    private Path dbDir;
    private DBInitializer dbInitializer;
    private SQLiteSummaryRepository summaryRepository;

    @BeforeEach
    void setUp() {
        dbDir = tempRoot.resolve("appdata");
        dbInitializer = new DBInitializer(dbDir);
        dbInitializer.initialize();
        summaryRepository = new SQLiteSummaryRepository(dbInitializer);
    }

    @Test
    void testSaveAndFindByIdMethods() {
        Summary summary = new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED);
        summaryRepository.save(summary);
        Assertions.assertEquals(summary, summaryRepository.findByID(1).get());
    }


    @Test
    void testFindAllMethod() {
        List<Summary> summaries = List.of(new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED), new Summary(2, "sample another title", "author 3, author 4", "sample summary of another paper", LocalDate.of(2024, 4, 23), "http:another_pdf_link", SummaryType.STRUCTURED));
        summaries.forEach(summary -> summaryRepository.save(summary));

        Assertions.assertEquals(summaries, summaryRepository.findAll());
    }

    @Test
    void testSaveAndFindByPdfLinkAndTypeMethods() {

        Summary summary = new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED);
        summaryRepository.save(summary);

        Summary secondSummary = summaryRepository.findByPdfLinkAndType(summary.pdfLink(), summary.summaryType()).get();

        Assertions.assertEquals(summary, secondSummary);
    }



}
