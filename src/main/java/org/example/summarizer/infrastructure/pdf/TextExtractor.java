package org.example.summarizer.infrastructure.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TextExtractor {

    // For self-added papers
    public String extractFromPath(Path pdfPath) throws IOException {
        Objects.requireNonNull(pdfPath, "pdfPath");
        try (InputStream inputStream = Files.newInputStream(pdfPath)) {
            return extractFromStream(inputStream);
        }
    }

    // For PDF's from arXiv
    public String extractFromUrl(String pdfUrl) throws IOException {
        Objects.requireNonNull(pdfUrl, "pdfUrl");
        URL url = URI.create(pdfUrl).toURL();
        try (InputStream inputStream = url.openStream()) {
            return extractFromStream(inputStream);
        }
    }

    // To extract data from both above
    public String extractFromStream(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");

        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            return textStripper.getText(document).trim();
        }
    }
}
