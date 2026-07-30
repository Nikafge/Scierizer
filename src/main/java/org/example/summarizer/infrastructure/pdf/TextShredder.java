package org.example.summarizer.infrastructure.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextShredder {
    private static final int APPROXIMATE_CHARS_PER_TOKEN = 4;

    public List<String> cutPaper(String content) {
        List<String> parts = new ArrayList<>();
        content = stripReferences(content);

        //Look for Reference chapter and cut it out of the paper
        Pattern pattern = Pattern.compile("(?m)^(\\d+)\\.?\\s+([A-Za-z][A-Za-z\\s]*)$");
        Matcher matcher = pattern.matcher(content);

        int startIndex = -1;

        while (matcher.find()) {
            if (startIndex != -1) {
                String sectionContent = content.substring(startIndex, matcher.start()).trim();

                if (!sectionContent.isEmpty()) {
                    parts.add(sectionContent);
                }
            }

            startIndex = matcher.start();
        }

        if (startIndex != -1 && startIndex < content.length()) {
            String lastContent = content.substring(startIndex).trim();
            if (!lastContent.isEmpty()) {
                parts.add(lastContent);
            }
        }

        return parts;
    }

    public List<String> cutPaper(String content, int chunkSizeTokens, int chunkOverlapTokens) {
        String text = stripReferences(content);
        int chunkSizeCharacters = Math.max(1, chunkSizeTokens) * APPROXIMATE_CHARS_PER_TOKEN;
        int overlapCharacters = Math.max(0, Math.min(chunkOverlapTokens, chunkSizeTokens - 1)) * APPROXIMATE_CHARS_PER_TOKEN;

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(text.length(), start + chunkSizeCharacters);
            chunks.add(text.substring(start, end).trim());

            if (end == text.length()) {
                break;
            }

            start = Math.max(end - overlapCharacters, start + 1);
        }

        return chunks.stream()
                .filter(chunk -> !chunk.isBlank())
                .toList();
    }

    private String stripReferences(String content) {
        Pattern refPattern = Pattern.compile("(?m)^(References?)\\s*$");
        Matcher refMatcher = refPattern.matcher(content);
        if (refMatcher.find()) {
            return content.substring(0, refMatcher.start()).trim();
        }
        return content.trim();
    }
}
