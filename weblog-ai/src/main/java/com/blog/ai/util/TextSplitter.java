package com.blog.ai.util;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {

    public static List<String> split(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty()) continue;
            if (para.length() <= chunkSize) {
                chunks.add(para);
            } else {
                chunks.addAll(splitLongParagraph(para, chunkSize, chunkOverlap));
            }
        }
        // Remove chunks that are too small
        chunks.removeIf(c -> c.length() < 50);
        return chunks;
    }

    private static List<String> splitLongParagraph(String paragraph, int chunkSize, int chunkOverlap) {
        List<String> sentences = splitSentences(paragraph);
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (current.length() + sentence.length() > chunkSize && current.length() > 0) {
                chunks.add(current.toString().trim());
                // Keep overlap from the end of current chunk
                String overlap = current.length() > chunkOverlap
                        ? current.substring(current.length() - chunkOverlap)
                        : current.toString();
                current = new StringBuilder(overlap);
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private static List<String> splitSentences(String text) {
        // Split by Chinese/English sentence terminators
        String[] parts = text.split("(?<=[。！？.!?])");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
