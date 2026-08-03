//package org.example.scierizer.infrastructure.persistence;
//
//import org.example.scierizer.domain.Paper;
//import org.example.scierizer.domain.Summary;
//import org.example.scierizer.viewmodel.SummaryType;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.nio.file.Path;
//import java.sql.Connection;
//import java.sql.Statement;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SQLiteSummaryRepositoryTest {
//    @TempDir
//    Path tempRoot;
//
//    private Path dbDir;
//    private DBInitializer dbInitializer;
//    private SQLiteSummaryRepository summaryRepository;
//
//    @BeforeEach
//    void setUp() {
//        dbDir = tempRoot.resolve("appdata");
//        dbInitializer = new DBInitializer(dbDir);
//        dbInitializer.initialize();
//        summaryRepository = new SQLiteSummaryRepository(dbInitializer);
//    }
//
//    @Test
//    void testSaveAndFindByIdMethods() {
//        Summary summary = new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED);
//        summaryRepository.save(summary);
//        Assertions.assertEquals(summary, summaryRepository.findByID(1).get());
//    }
//
//
//    @Test
//    void testFindAllMethod() {
//        List<Summary> summaries = List.of(new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED), new Summary(2, "sample another title", "author 3, author 4", "sample summary of another paper", LocalDate.of(2024, 4, 23), "http:another_pdf_link", SummaryType.STRUCTURED));
//        summaries.forEach(summary -> summaryRepository.save(summary));
//
//        Assertions.assertEquals(summaries, summaryRepository.findAll());
//    }
//
//    @Test
//    void findAllReturnsEmptyListForMigratedEmptySummaryTable() throws Exception {
//        DBInitializer legacyDbInitializer = new DBInitializer(tempRoot.resolve("legacy-appdata"));
//        try (Connection connection = legacyDbInitializer.getConnection();
//             Statement statement = connection.createStatement()) {
//            statement.execute("""
//                CREATE TABLE summary (
//                    id INTEGER PRIMARY KEY AUTOINCREMENT,
//                    summary TEXT NOT NULL,
//                    pdf_link TEXT,
//                    title TEXT,
//                    authors TEXT,
//                    date_published TEXT
//                )
//            """);
//        }
//
//        legacyDbInitializer.initialize();
//        SQLiteSummaryRepository legacySummaryRepository = new SQLiteSummaryRepository(legacyDbInitializer);
//
//        assertEquals(List.of(), legacySummaryRepository.findAll());
//    }
//
//    @Test
//    void saveWithZeroIdCreatesNewRowsInsteadOfOverwriting() {
//        Summary firstSummary = new Summary(0, "first title", "author 1", "first summary", LocalDate.of(2026, 5, 3), "http:first_pdf_link", SummaryType.STRUCTURED);
//        Summary secondSummary = new Summary(0, "second title", "author 2", "second summary", LocalDate.of(2026, 5, 4), "http:second_pdf_link", SummaryType.TLDR);
//
//        summaryRepository.save(firstSummary);
//        summaryRepository.save(secondSummary);
//
//        List<Summary> savedSummaries = summaryRepository.findAll();
//        assertEquals(2, savedSummaries.size());
//        assertEquals("first summary", savedSummaries.get(0).summary());
//        assertEquals("second summary", savedSummaries.get(1).summary());
//        assertEquals(1, savedSummaries.get(0).id());
//        assertEquals(2, savedSummaries.get(1).id());
//    }
//
//    @Test
//    void deleteByIdRemovesOnlySelectedSummary() {
//        Summary firstSummary = new Summary(1, "first title", "author 1", "first summary", LocalDate.of(2026, 5, 3), "http:first_pdf_link", SummaryType.STRUCTURED);
//        Summary secondSummary = new Summary(2, "second title", "author 2", "second summary", LocalDate.of(2026, 5, 4), "http:second_pdf_link", SummaryType.TLDR);
//        summaryRepository.save(firstSummary);
//        summaryRepository.save(secondSummary);
//
//        summaryRepository.deleteById(1);
//
//        assertTrue(summaryRepository.findByID(1).isEmpty());
//        assertEquals(List.of(secondSummary), summaryRepository.findAll());
//    }
//
//    @Test
//    void testSaveAndFindByPdfLinkAndTypeMethods() {
//
//        Summary summary = new Summary(1, "sample title", "author 1, author 2", "sample summary of some paper", LocalDate.of(2026, 5, 3), "http:pdf_link", SummaryType.STRUCTURED);
//        summaryRepository.save(summary);
//
//        Summary secondSummary = summaryRepository.findByPdfLinkAndType(summary.pdfLink(), summary.summaryType()).get();
//
//        Assertions.assertEquals(summary, secondSummary);
//    }
//
//
//
//}
