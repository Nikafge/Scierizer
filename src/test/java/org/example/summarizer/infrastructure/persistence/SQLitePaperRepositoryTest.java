//package org.example.summarizer.infrastructure.persistence;
//
//import org.example.summarizer.domain.Paper;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import java.nio.file.Path;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SQLitePaperRepositoryTest {
//
//    @TempDir
//    Path tempRoot;
//
//    private Path dbDir;
//    private DBInitializer dbInitializer;
//    private SQLitePaperRepository paperRepository;
//
//    @BeforeEach
//    void setUp() {
//        dbDir = tempRoot.resolve("appdata");
//        dbInitializer = new DBInitializer(dbDir);
//        dbInitializer.initialize();
//        paperRepository = new SQLitePaperRepository(dbInitializer);
//    }
//
//    @Test
//    void testSaveAndFindByIdMethods() {
//
//        Paper paper = new Paper("sample title", 1, "This is abstract test", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1, author 2", "http:pdf_link");
//        paperRepository.save(paper);
//        Assertions.assertEquals(paper, paperRepository.findById(1).get());
//    }
//
//    @Test
//    void testFindAllMethod() {
//        List<Paper> papers = List.of(new Paper("sample title", 1, "This is abstract test", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1, author 2", "http:pdf_link"), new Paper("second sample title", 2, "This is another abstract test", LocalDate.of(2022, 2, 20), LocalDate.of(2022, 9, 15), "author 3, author 4", "http:another+pdf_link"));
//        papers.forEach(paper -> paperRepository.save(paper));
//        Assertions.assertEquals(papers, paperRepository.findAll());
//    }
//
//    @Test
//    void saveWithZeroIdCreatesNewRowsInsteadOfOverwriting() {
//        Paper firstPaper = new Paper("first title", 0, "first abstract", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1", "http:first_pdf_link");
//        Paper secondPaper = new Paper("second title", 0, "second abstract", LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 3), "author 2", "http:second_pdf_link");
//
//        paperRepository.save(firstPaper);
//        paperRepository.save(secondPaper);
//
//        List<Paper> savedPapers = paperRepository.findAll();
//        assertEquals(2, savedPapers.size());
//        assertEquals("first title", savedPapers.get(0).title());
//        assertEquals("second title", savedPapers.get(1).title());
//        assertEquals(1, savedPapers.get(0).id());
//        assertEquals(2, savedPapers.get(1).id());
//    }
//
//    @Test
//    void deleteByIdRemovesOnlySelectedPaper() {
//        Paper firstPaper = new Paper("first title", 1, "first abstract", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1", "http:first_pdf_link");
//        Paper secondPaper = new Paper("second title", 2, "second abstract", LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 3), "author 2", "http:second_pdf_link");
//        paperRepository.save(firstPaper);
//        paperRepository.save(secondPaper);
//
//        paperRepository.deleteById(1);
//
//        assertTrue(paperRepository.findById(1).isEmpty());
//        assertEquals(List.of(secondPaper), paperRepository.findAll());
//    }
//}
//
