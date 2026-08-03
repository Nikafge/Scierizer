//package org.example.scierizer.viewmodel;
//
//import org.example.scierizer.domain.Paper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//class MainViewModelTest {
//
//    @TempDir
//    Path tempDirectory;
//
//    @Test
//    void createLocalPdfPaperUsesFileMetadataAndAbsolutePath() throws Exception {
//        Path pdfPath = tempDirectory.resolve("uploaded-paper.pdf");
//        Files.writeString(pdfPath, "%PDF-1.7");
//        MainViewModel viewModel = new MainViewModel(null);
//
//        Paper paper = viewModel.createLocalPdfPaper(pdfPath);
//
//        assertEquals("uploaded-paper", paper.title());
//        assertEquals(0, paper.id());
//        assertEquals("Local PDF", paper.authors());
//        assertEquals(pdfPath.toAbsolutePath().normalize().toString(), paper.pdfLink());
//        assertEquals("No abstract is available for this uploaded PDF.", paper.abstractText());
//    }
//
//    @Test
//    void createLocalPdfPaperRejectsNonPdfFiles() {
//        MainViewModel viewModel = new MainViewModel(null);
//
//        assertThrows(
//                IllegalArgumentException.class,
//                () -> viewModel.createLocalPdfPaper(tempDirectory.resolve("notes.txt"))
//        );
//    }
//}
