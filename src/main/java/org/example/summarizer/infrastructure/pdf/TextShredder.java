package org.example.summarizer.infrastructure.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextShredder {
    public static List<String> cutPaper(String content) {
        List<String> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?m)^(\\d+)\\.?\\s+([A-Za-z][A-Za-z\\s]*)$");
        Matcher matcher = pattern.matcher(content);

        int index = -1;

        while (matcher.find()) {

            if (index != -1) {
                String sectionContent = content.substring(index, matcher.start()).trim();

                if (!sectionContent.isEmpty()) {
                    parts.add(sectionContent);
                }
            }

            index = matcher.end();
        }

        if (index != -1 && index < content.length()) {
            String lastContent = content.substring(index).trim();
            if (!lastContent.isEmpty()) {
                parts.add(lastContent);
            }
        }


        return parts;
    }
}