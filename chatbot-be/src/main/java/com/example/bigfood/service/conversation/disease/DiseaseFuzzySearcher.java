package com.example.bigfood.service.conversation.disease;

import com.example.bigfood.entity.Disease;
import com.example.bigfood.repository.DiseaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;
import com.example.bigfood.service.conversation.util.TextNormalizer;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiseaseFuzzySearcher {
    
    private final DiseaseRepository diseaseRepository;
    private final TextNormalizer textNormalizer;
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    /**
     * Tìm bệnh với fuzzy search
     */
    public Optional<Disease> findByFuzzySearch(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return Optional.empty();
        }
        
        String normalizedInput = textNormalizer.normalize(userInput);
        List<Disease> allDiseases = diseaseRepository.findAll();
        
        Disease bestMatch = null;
        double bestSimilarity = 0.0;
        
        for (Disease disease : allDiseases) {
            String normalizedDiseaseName = textNormalizer.normalize(disease.getName());
            
            double similarity = calculateSimilarity(normalizedInput, normalizedDiseaseName);
            
            if (similarity > bestSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                bestSimilarity = similarity;
                bestMatch = disease;
            }
        }
        
        if (bestMatch != null) {
            log.info("Fuzzy search: '{}' matched '{}' with similarity {:.2f}", 
                     userInput, bestMatch.getName(), bestSimilarity);
        }
        
        return Optional.ofNullable(bestMatch);
    }
    
    /**
     * Tìm bệnh trong message
     */
    public Optional<Disease> findInMessage(String normalizedMessage) {
        // Thử tìm khớp chính xác trước
        Optional<Disease> exactMatch = diseaseRepository.findAll().stream()
            .filter(disease -> {
                String normalizedName = textNormalizer.normalize(disease.getName());
                return !normalizedName.isBlank() && normalizedMessage.contains(normalizedName);
            })
            .findFirst();
        
        if (exactMatch.isPresent()) {
            log.info("Exact match found: {}", exactMatch.get().getName());
            return exactMatch;
        }
        
        // Nếu không có, dùng fuzzy search
        return findByFuzzySearch(normalizedMessage);
    }
    
    /**
     * Tính độ tương đồng giữa 2 chuỗi
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        
        // Kiểm tra contain
        if (s1.contains(s2) || s2.contains(s1)) {
            return 0.9;
        }
        
        // Tính Levenshtein distance
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = levenshtein.apply(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }
}