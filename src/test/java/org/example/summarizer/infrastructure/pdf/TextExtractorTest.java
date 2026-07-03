package org.example.summarizer.infrastructure.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TextExtractorTest {

    @Test
    void extractsTextFromPdfStream() throws Exception {
        byte[] pdfBytes;
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 12);
                contentStream.newLineAtOffset(72, 720);
                contentStream.showText("Hello PDF");
                contentStream.endText();
            }

            document.save(outputStream);
            pdfBytes = outputStream.toByteArray();
        }

        TextExtractor textExtractor = new TextExtractor();


        Optional<String> extractedText = textExtractor.extractFromStream(new ByteArrayInputStream(pdfBytes));

        assertTrue(extractedText.isPresent());
        assertEquals("Hello PDF", extractedText.get());
    }
}
