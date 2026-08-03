//package org.example.scierizer.service;
//
//import org.example.scierizer.domain.Summary;
//import org.example.scierizer.viewmodel.SummaryType;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class ReportExportServiceTest {
//
//    @TempDir
//    Path tempDirectory;
//
//    @Test
//    void exportSummaryWritesConfiguredFormat() throws Exception {
//        ReportExportService reportExportService = new ReportExportService();
//        Summary summary = new Summary(
//                0,
//                "A/B Summary",
//                "Author",
//                "Summary text",
//                LocalDate.of(2026, 7, 28),
//                "paper.pdf",
//                SummaryType.TLDR
//        );
//
//        Path outputPath = reportExportService.exportSummary(summary, tempDirectory, "Text");
//
//        assertEquals("A_B Summary - tldr.txt", outputPath.getFileName().toString());
//        assertTrue(Files.exists(outputPath));
//        String exportedContent = Files.readString(outputPath);
//        assertTrue(exportedContent.contains("A/B Summary"));
//        assertTrue(exportedContent.contains("Summary type: tldr"));
//        assertTrue(exportedContent.contains("Authors: Author"));
//        assertTrue(exportedContent.contains("Published: 2026-07-28"));
//        assertTrue(exportedContent.contains("Source: paper.pdf"));
//        assertTrue(exportedContent.contains("Summary text"));
//    }
//
//    @Test
//    void exportSummaryUsesUniqueFileNameWhenFileAlreadyExists() throws Exception {
//        ReportExportService reportExportService = new ReportExportService();
//        Summary summary = new Summary(
//                0,
//                "Repeated Summary",
//                "Author",
//                "Summary text",
//                LocalDate.of(2026, 7, 28),
//                "paper.pdf",
//                SummaryType.TLDR
//        );
//
//        Path firstOutputPath = reportExportService.exportSummary(summary, tempDirectory, "Markdown");
//        Path secondOutputPath = reportExportService.exportSummary(summary, tempDirectory, "Markdown");
//
//        assertEquals("Repeated Summary - tldr.md", firstOutputPath.getFileName().toString());
//        assertEquals("Repeated Summary - tldr (2).md", secondOutputPath.getFileName().toString());
//        assertTrue(Files.exists(firstOutputPath));
//        assertTrue(Files.exists(secondOutputPath));
//    }
//
//    @Test
//    void exportSummaryWritesMarkdownReport() throws Exception {
//        ReportExportService reportExportService = new ReportExportService();
//        Summary summary = new Summary(
//                0,
//                "Markdown Summary",
//                "Author",
//                "Summary text",
//                LocalDate.of(2026, 7, 28),
//                "paper.pdf",
//                SummaryType.STRUCTURED
//        );
//
//        Path outputPath = reportExportService.exportSummary(summary, tempDirectory, "Markdown");
//
//        assertEquals("Markdown Summary - structured.md", outputPath.getFileName().toString());
//        assertEquals("""
//                # Markdown Summary
//
//                - Summary type: structured
//                - Authors: Author
//                - Published: 2026-07-28
//                - Source: paper.pdf
//
//                ## Summary
//
//                Summary text
//                """, Files.readString(outputPath));
//    }
//}
