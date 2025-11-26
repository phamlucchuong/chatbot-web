package com.example.bigfood.service.conversation.util;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util. Locale;
import java.util.stream.Collectors;

@Component
public class TextNormalizer {
    
    /**
     * Chuẩn hóa văn bản: lowercase, trim
     */
    public String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        
        // Chỉ làm lowercase và trim, KHÔNG xóa dấu
        String normalized = input.toLowerCase(Locale.ROOT).trim();
        
        // Loại bỏ khoảng trắng thừa
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized;
    }
    
    /**
     * Chuẩn hóa list keywords
     */
    public List<String> normalizeList(List<String> keywords) {
        return keywords.stream()
                .map(this::normalize)
                .collect(Collectors. toList());
    }
}