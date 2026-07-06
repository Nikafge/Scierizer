package org.example.summarizer.infrastructure.pdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class TextShredderTest {

    @Test
    void testPartsCutting() throws IOException {

        String content = new TextExtractor().extractFromPath(Path.of("C:\\\\Users\\\\marcs\\\\Downloads\\\\2607.01390v1.pdf")).get();
        List<String> shreddedText = TextShredder.cutPaper(content);

        System.out.println(shreddedText);

        Assertions.assertEquals(8, shreddedText.size());

    }


}