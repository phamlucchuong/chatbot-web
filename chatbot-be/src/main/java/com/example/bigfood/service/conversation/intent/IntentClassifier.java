package com.example.bigfood.service.conversation.intent;

import com.example.bigfood.service.conversation.session.SessionState;
import com.example.bigfood.service.conversation.util.MedicalKeywordMatcher;
import com.example. bigfood.service.conversation.util.TextNormalizer;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentClassifier {
    
    private final MedicalKeywordMatcher keywordMatcher;
    private final TextNormalizer textNormalizer;
    
    public UserIntent classify(String message, SessionState state) {
        if (message == null || message.isBlank()) {
            return UserIntent.OTHER;
        }
        
        String normalized = textNormalizer. normalize(message);
        log.info("========== CLASSIFY INTENT ==========");
        log. info("Original: {}", message);
        log. info("Normalized: {}", normalized);
        
        // ====== THỨ TỰ ƯU TIÊN CHÍNH XÁC ======
        
        // 1️⃣ OFF-TOPIC - ƯU TIÊN CAO (thời tiết, phim, game...)
        if (keywordMatcher.isOffTopic(message)) {
            log.info("✅ RESULT: OFF_TOPIC\n");
            return UserIntent. OFF_TOPIC;
        }
        
        // 2️⃣ ⭐⭐⭐ ASK_MEDICAL_CONTEXT - ƯU TIÊN RẤT CAO
        // PHẢI TRƯỚC GREETING, DISEASE_INFO, SYMPTOM
        if (keywordMatcher. isAskingMedicalContext(message)) {
            log.info("✅ RESULT: ASK_MEDICAL_CONTEXT\n");
            return UserIntent.ASK_MEDICAL_CONTEXT;
        }
        
        // 3️⃣ DECLARE_DISEASE - Khai báo bệnh
        if (keywordMatcher.isDeclaringDisease(message)) {
            log.info("✅ RESULT: DECLARE_DISEASE\n");
            return UserIntent.DECLARE_DISEASE;
        }
        
        // 4️⃣ ASK_DISEASE_INFO - Hỏi thông tin bệnh
        if (keywordMatcher.isAskingDiseaseInfo(message)) {
            log.info("✅ RESULT: ASK_DISEASE_INFO\n");
            return UserIntent.ASK_DISEASE_INFO;
        }
        
        // 5️⃣ VAGUE_COMPLAINT - Than phiền chung chung
        if (keywordMatcher.isVagueComplaint(message)) {
            log.info("✅ RESULT: VAGUE_COMPLAINT\n");
            return UserIntent.VAGUE_COMPLAINT;
        }
        
        // 6️⃣ SYMPTOM_DESCRIPTION - Mô tả triệu chứng cụ thể
        if (keywordMatcher.hasSpecificSymptoms(message)) {
            log.info("✅ RESULT: SYMPTOM_DESCRIPTION\n");
            return UserIntent.SYMPTOM_DESCRIPTION;
        }
        
        // 7️⃣ FAREWELL - Tạm biệt
        if (isFarewell(message)) {
            log.info("✅ RESULT: FAREWELL\n");
            return UserIntent.FAREWELL;
        }
        
        // 8️⃣ THANKS - Cảm ơn
        if (isThanks(message)) {
            log.info("✅ RESULT: THANKS\n");
            return UserIntent.THANKS;
        }
        
        // 9️⃣ GREETING - Lời chào (PHẢI ĐẶT CUỐI CÙNG!)
        if (isGreeting(message)) {
            log.info("✅ RESULT: GREETING\n");
            return UserIntent.GREETING;
        }
        
        log.info("✅ RESULT: OTHER\n");
        return UserIntent.OTHER;
    }
    
    private boolean isGreeting(String message) {
        String normalized = textNormalizer.normalize(message);
        
        // Chỉ coi là greeting nếu message CHỈ là lời chào (không phải câu hỏi phức tạp)
        if (normalized.length() > 30) {
            return false; // Message quá dài, không phải greeting đơn giản
        }
        
        // Kiểm tra có keyword y tế hoặc hành động không
        int medicalScore = keywordMatcher. calculateMedicalScore(message);
        int offTopicScore = keywordMatcher.calculateOffTopicScore(message);
        
        if (medicalScore > 0 || offTopicScore > 0) {
            return false; // Có từ khóa khác, không phải greeting thuần túy
        }
        
        String[] greetingKeywords = {
            "xin chào", "chào", "hello", "hi", "halo", "hay ho",
            "buổi chiều", "buổi sáng", "chào bạn", "chào ban", "buổi tối"
        };
        
        for (String keyword : greetingKeywords) {
            if (normalized.contains(keyword)) {
                log.debug("Greeting keyword detected: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    private boolean isFarewell(String message) {
        String normalized = textNormalizer.normalize(message);
        String[] farewellKeywords = {
            "tạm biệt", "bye", "goodbye", "đã chào", "hẹn gặp lại",
            "tạm biệt bạn", "cút đây", "dừng đây"
        };
        
        for (String keyword : farewellKeywords) {
            if (normalized.contains(keyword)) {
                log.debug("Farewell keyword detected: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    private boolean isThanks(String message) {
        String normalized = textNormalizer.normalize(message);
        String[] thanksKeywords = {
            "cảm ơn", "thank you", "thanks", "cảm ơn nhiều",
            "rất cảm ơn"
        };
        
        for (String keyword : thanksKeywords) {
            if (normalized. contains(keyword)) {
                log.debug("Thanks keyword detected: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    public MedicalKeywordMatcher getMedicalKeywordMatcher() {
        return keywordMatcher;
    }
}