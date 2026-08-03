//package org.example.scierizer.infrastructure.pdf;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.w3c.dom.Text;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.Optional;
//
//public class TextExtractorWithRealFileTest {
//
//    @Test
//    void displayFileContent() throws IOException {
//        TextExtractor textExtractor = new TextExtractor();
//        Optional<String> content = textExtractor.extractFromPath(Path.of("C:\\Users\\marcs\\Downloads\\2607.01390v1.pdf"));
//        Assertions.assertTrue(content.isPresent());
//        System.out.println(content.get());
//    }
//    @Test
//    void displayOnlineFileContent() throws IOException, InterruptedException {
//        TextExtractor textExtractor = new TextExtractor();
////        Optional<String> content = textExtractor.extractFromUrl("https://arxiv.org/pdf/2607.01390");
//        Optional<String> content = textExtractor.extractFromUrl("https://arxiv.org/pdf/2607.02396");
//        Assertions.assertTrue(content.isPresent());
//        System.out.println(content.get());
//    }
//}