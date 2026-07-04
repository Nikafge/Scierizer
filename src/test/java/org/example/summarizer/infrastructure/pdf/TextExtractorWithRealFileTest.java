package org.example.summarizer.infrastructure.pdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class TextExtractorWithRealFileTest {

    @Test
    void displayFileContent() throws IOException {
        TextExtractor textExtractor = new TextExtractor();
        Optional<String> content = textExtractor.extractFromPath(Path.of("C:\\Users\\marcs\\Downloads\\2607.01390v1.pdf"));
        Assertions.assertTrue(content.isPresent());
        System.out.println(content.get());
    }



}
