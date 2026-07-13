package org.example.summarizer.infrastructure.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextShredder {
    public List<String> cutPaper(String content) {
        List<String> parts = new ArrayList<>();

        //Look for Reference chapter and cut it out of the paper
        Pattern refPattern = Pattern.compile("(?m)^(References?)\\s*$");
        Matcher refMatcher = refPattern.matcher(content);
        if (refMatcher.find()) {
            content = content.substring(0, refMatcher.start()).trim();
        }

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
}