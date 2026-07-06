package org.example.summarizer.infrastructure.persistence;

import org.example.summarizer.domain.Paper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLitePaperRepositoryTest {

    @TempDir
    Path tempRoot;

    private Path dbDir;
    private DBInitializer dbInitializer;
    private SQLitePaperRepository paperRepository;

    @BeforeEach
    void setUp() {
        dbDir = tempRoot.resolve("appdata");
        dbInitializer = new DBInitializer(dbDir);
        dbInitializer.initialize();
        paperRepository = new SQLitePaperRepository(dbInitializer);
    }

    @Test
    void testSaveAndFindByIdMethods() {

        Paper paper = new Paper("sample title", 1, "This is abstract test", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1, author 2", "http:pdf_link");
        paperRepository.save(paper);
        Assertions.assertEquals(paper, paperRepository.findById(1).get());
    }

    @Test
    void testFindAllMethod() {
        List<Paper> papers = List.of(new Paper("sample title", 1, "This is abstract test", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2), "author 1, author 2", "http:pdf_link"), new Paper("second sample title", 2, "This is another abstract test", LocalDate.of(2022, 2, 20), LocalDate.of(2022, 9, 15), "author 3, author 4", "http:another+pdf_link"));
        papers.forEach(paper -> paperRepository.save(paper));
        Assertions.assertEquals(papers, paperRepository.findAll());
    }
}

