//package org.example.scierizer.infrastructure.pdf;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
//import org.example.scierizer.domain.settings.AppSettings;
//import org.example.scierizer.domain.settings.ModelSettings;
//import org.example.scierizer.infrastructure.ollama.OllamaClient;
//import org.junit.jupiter.api.Test;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class TextExtractorTest {
//
//    @Test
//    void extractsTextFromPdfStream() throws Exception {
//        byte[] pdfBytes;
//        try (PDDocument document = new PDDocument();
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
//                contentStream.beginText();
//                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 12);
//                contentStream.newLineAtOffset(72, 720);
//                contentStream.showText("Hello PDF");
//                contentStream.endText();
//            }
//
//            document.save(outputStream);
//            pdfBytes = outputStream.toByteArray();
//        }
//
//        TextExtractor textExtractor = new TextExtractor();
//
//
//        Optional<String> extractedText = textExtractor.extractFromStream(new ByteArrayInputStream(pdfBytes));
//
//        assertTrue(extractedText.isPresent());
//        assertEquals("Hello PDF", extractedText.get());
//    }
//
//    @Test
//    void extractsTextFromPdfStreamWithUnlimitedOcr() throws Exception {
//        byte[] pdfBytes = createPdfBytes("Rendered PDF");
//        FakeOllamaClient ollamaClient = new FakeOllamaClient();
//
//        Optional<String> extractedText = new TextExtractor().extractFromStreamWithUnlimitedOcr(
//                new ByteArrayInputStream(pdfBytes),
//                ollamaClient,
//                AppSettings.defaults().model()
//        );
//
//        assertTrue(extractedText.isPresent());
//        assertEquals("OCR page text", extractedText.get());
//        assertEquals(1, ollamaClient.pageNumber);
//        assertTrue(ollamaClient.base64PngImage.length() > 100);
//    }
//
//    private byte[] createPdfBytes(String text) throws Exception {
//        try (PDDocument document = new PDDocument();
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
//                contentStream.beginText();
//                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 12);
//                contentStream.newLineAtOffset(72, 720);
//                contentStream.showText(text);
//                contentStream.endText();
//            }
//
//            document.save(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    private static class FakeOllamaClient extends OllamaClient {
//        private String base64PngImage;
//        private int pageNumber;
//
//        @Override
//        public String extractTextFromImage(String base64Image, int pageNumber, ModelSettings modelSettings) {
//            this.base64PngImage = base64Image;
//            this.pageNumber = pageNumber;
//            return "OCR page text";
//        }
//    }
//}
