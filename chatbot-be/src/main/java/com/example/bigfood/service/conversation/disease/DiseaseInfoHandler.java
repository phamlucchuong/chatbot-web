package com.example.bigfood.service.conversation.disease;

import com.example.bigfood.entity.Disease;
import com.example.bigfood.repository.DiseaseRepository;
import com.example.bigfood.service.conversation.response.PromptTemplates;
import com.example.bigfood.service.conversation.response.ResponseGenerator;
import com.example.bigfood.service.conversation.session.SessionManager;
import com.example.bigfood.service.conversation.session.SessionState;
import com.example.bigfood.service.conversation.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiseaseInfoHandler {
    
    private final DiseaseRepository diseaseRepository;
    private final DiseaseFuzzySearcher fuzzySearcher;
    private final ResponseGenerator responseGenerator;
    private final SessionManager sessionManager;
    private final TextNormalizer textNormalizer;
    
    private static final List<String> DISEASE_INFO_QUESTION_KEYWORDS = List.of(
        "thong tin", "cho toi biet", "la gi", "mo ta", "trieu chung",
        "nguyen nhan", "phong ngua", "dieu tri", "cach chua", "cach phong"
    );

    /**
     * Xử lý khi user tự khai báo bệnh: "Tôi bị tiểu đường"
     * Trả về null nếu không phải câu khai báo
     */
    public String handleDeclaredDisease(String conversationId, String userMessage, SessionState state) {
        String normalized = textNormalizer.normalize(userMessage);
        
        // Kiểm tra có keyword khai báo không
        if (!containsDiseaseDeclaration(normalized)) {
            return null;
        }
        
        // Tìm tên bệnh trong message
        Optional<Disease> diseaseOpt = fuzzySearcher.findInMessage(normalized);
        
        // QUAN TRỌNG: Nếu không tìm thấy bệnh → trả về null để xử lý như triệu chứng
        if (diseaseOpt.isEmpty()) {
            return null;
        }
        
        Disease disease = diseaseOpt.get();
        
        // Cập nhật state: đánh dấu đã chẩn đoán
        state.setDiagnosed(true);
        state.setDiagnosedDiseaseId(disease.getId());
        state.setDiagnosedDiseaseName(disease.getName());
        state.getAskedTopics().clear(); // Reset topic đã hỏi
        sessionManager.updateState(conversationId, state);
        
        // Trả lời theo topic user hỏi (nếu có)
        return respondWithTopic(conversationId, disease, extractTopic(userMessage), state);
    }
    
    /**
     * Xử lý khi user hỏi trực tiếp về bệnh: "Bệnh tiểu đường là gì?"
     * Trả về null nếu không phải câu hỏi về bệnh
     */
    public String handleDirectQuestion(String conversationId, String userMessage, SessionState state) {
        String normalized = textNormalizer.normalize(userMessage);
        
        // Kiểm tra có phải câu hỏi về bệnh không
        if (!isDiseaseInfoQuestion(normalized)) {
            return null;
        }
        
        // Tìm tên bệnh trong message
        Optional<Disease> diseaseOpt = fuzzySearcher.findInMessage(normalized);
        
        if (diseaseOpt.isEmpty()) {
            // Nếu đã chẩn đoán rồi, có thể user hỏi về bệnh đó
            if (state.isDiagnosed()) {
                return handleTopicQuestion(conversationId, userMessage, state);
            }
            return "Xin lỗi, tôi không tìm thấy thông tin về bệnh bạn đang hỏi trong hệ thống. " +
                   "Bạn có thể viết lại tên bệnh chính xác hơn được không?";
        }
        
        Disease disease = diseaseOpt.get();
        
        // Cập nhật state
        state.setDiagnosed(true);
        state.setDiagnosedDiseaseId(disease.getId());
        state.setDiagnosedDiseaseName(disease.getName());
        state.getAskedTopics().clear();
        sessionManager.updateState(conversationId, state);
        
        return respondWithTopic(conversationId, disease, extractTopic(userMessage), state);
    }
    
    /**
     * Xử lý khi user hỏi về topic của bệnh đã chẩn đoán
     */
    public String handleTopicQuestion(String conversationId, String userMessage, SessionState state) {
        if (!state.isDiagnosed()) {
            return "Bạn cần mô tả triệu chứng để tôi có thể chẩn đoán trước nhé!";
        }
        
        Disease disease = diseaseRepository.findById(state.getDiagnosedDiseaseId()).orElse(null);
        if (disease == null) {
            return "Xin lỗi, tôi không tìm thấy thông tin bệnh này trong hệ thống.";
        }
        
        String topic = extractTopic(userMessage);
        return respondWithTopic(conversationId, disease, topic, state);
    }
    
    /**
     * Trả lời theo topic cụ thể
     */
    private String respondWithTopic(String conversationId, Disease disease, String topic, SessionState state) {
        // Kiểm tra topic đã hỏi chưa
        if (state.getAskedTopics().contains(topic)) {
            return handleRepeatedTopic(disease, topic);
        }
        
        state.getAskedTopics().add(topic);
        sessionManager.updateState(conversationId, state);
        
        return answerTopic(disease, topic, state);
    }
    
    /**
     * Trả lời topic cụ thể
     */
    private String answerTopic(Disease disease, String topic, SessionState state) {
        String topicData = getTopicData(disease, topic);
        String topicName = getTopicName(topic);
        
        Set<String> remainingTopics = new HashSet<>(
            Arrays.asList("description", "symptoms", "causes", "preventions", "treatment")
        );
        remainingTopics.removeAll(state.getAskedTopics());
        
        String remainingStr = remainingTopics.stream()
            .map(this::getTopicName)
            .collect(Collectors.joining(", "));
        
        return responseGenerator.generate(
            PromptTemplates.TOPIC_INFO,
            disease.getName(),
            topicName,
            topicData,
            topicName,
            remainingStr.isEmpty() ? "điều gì khác" : remainingStr
        );
    }
    
    /**
     * Xử lý khi hỏi lại topic đã hỏi
     */
    private String handleRepeatedTopic(Disease disease, String topic) {
        String topicData = getTopicData(disease, topic);
        String topicName = getTopicName(topic);
        
        return String.format(
            "Như tôi đã đề cập trước đó về %s:\n\n%s\n\n" +
            "Bạn có muốn biết thêm về các khía cạnh khác của bệnh không?",
            topicName.toLowerCase(), topicData
        );
    }
    
    /**
     * Trích xuất topic từ câu hỏi
     */
    public String extractTopic(String message) {
        String normalized = textNormalizer.normalize(message);
        
        Map<String, Integer> topicScores = new HashMap<>();
        topicScores.put("description", 0);
        topicScores.put("symptoms", 0);
        topicScores.put("causes", 0);
        topicScores.put("preventions", 0);
        topicScores.put("treatment", 0);
        
        // Triệu chứng
        String[] symptomKeywords = {
            "trieu chung", "dau hieu", "bieu hien", "co dau hieu gi",
            "co trieu chung gi", "nhung trieu chung", "cac trieu chung"
        };
        for (String kw : symptomKeywords) {
            if (normalized.contains(kw)) {
                topicScores.put("symptoms", topicScores.get("symptoms") + 3);
            }
        }
        
        // Mô tả
        String[] descriptionKeywords = {
            "la gi", "mo ta", "the nao", "gioi thieu", "benh nay",
            "dinh nghia", "khai niem"
        };
        for (String kw : descriptionKeywords) {
            if (normalized.contains(kw)) {
                topicScores.put("description", topicScores.get("description") + 3);
            }
        }
        
        // Nguyên nhân
        String[] causesKeywords = {
            "nguyen nhan", "tai sao", "vi sao", "do dau", "gay ra", "bi boi"
        };
        for (String kw : causesKeywords) {
            if (normalized.contains(kw)) {
                topicScores.put("causes", topicScores.get("causes") + 3);
            }
        }
        
        // Phòng ngừa
        String[] preventionKeywords = {
            "phong ngua", "phong tranh", "tranh", "lam sao tranh", "cach phong"
        };
        for (String kw : preventionKeywords) {
            if (normalized.contains(kw)) {
                topicScores.put("preventions", topicScores.get("preventions") + 3);
            }
        }
        
        // Điều trị
        String[] treatmentKeywords = {
            "dieu tri", "chua", "chua tri", "cach chua", "lam sao khoi", "uong thuoc gi"
        };
        for (String kw : treatmentKeywords) {
            if (normalized.contains(kw)) {
                topicScores.put("treatment", topicScores.get("treatment") + 3);
            }
        }
        
        String bestTopic = "general";
        int maxScore = 0;
        
        for (Map.Entry<String, Integer> entry : topicScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestTopic = entry.getKey();
            }
        }
        
        log.info("Topic detected: {} (score: {})", bestTopic, maxScore);
        return maxScore == 0 ? "general" : bestTopic;
    }
    
    private String getTopicData(Disease disease, String topic) {
        return switch (topic) {
            case "description" -> disease.getDescription();
            case "symptoms" -> disease.getSymptoms();
            case "causes" -> disease.getCauses();
            case "preventions" -> disease.getPreventions();
            case "treatment" -> disease.getTreatment();
            default -> buildFullContext(disease);
        };
    }
    
    private String getTopicName(String topic) {
        return switch (topic) {
            case "description" -> "mô tả bệnh";
            case "symptoms" -> "triệu chứng";
            case "causes" -> "nguyên nhân";
            case "preventions" -> "cách phòng ngừa";
            case "treatment" -> "cách điều trị";
            default -> "thông tin chung";
        };
    }
    
    private String buildFullContext(Disease disease) {
        return String.format("""
            Tên: %s
            Mô tả: %s
            Triệu chứng: %s
            Nguyên nhân: %s
            Phòng ngừa: %s
            Điều trị: %s
            """, disease.getName(), disease.getDescription(), disease.getSymptoms(),
            disease.getCauses(), disease.getPreventions(), disease.getTreatment());
    }
    
    /**
     * Kiểm tra message có chứa keyword khai báo bệnh không
     * CHÚ Ý: Phải có CẢ keyword khai báo VÀ tên bệnh hợp lệ
     */
    private boolean containsDiseaseDeclaration(String normalized) {
        String[] keywords = {"bi", "dang bi", "dang mac", "mac", "chan doan", 
                            "duoc chan doan", "toi bi"};
        
        boolean hasKeyword = false;
        for (String kw : keywords) {
            if (normalized.contains(kw)) {
                hasKeyword = true;
                break;
            }
        }
        
        if (!hasKeyword) return false;
        
        // QUAN TRỌNG: Kiểm tra xem có tên bệnh trong DB không
        Optional<Disease> diseaseOpt = fuzzySearcher.findInMessage(normalized);
        return diseaseOpt.isPresent();
    }
    
    /**
     * Kiểm tra có phải câu hỏi về bệnh không
     */
    private boolean isDiseaseInfoQuestion(String normalized) {
        boolean hasInfoKeyword = DISEASE_INFO_QUESTION_KEYWORDS.stream()
            .map(textNormalizer::normalize)
            .anyMatch(normalized::contains);
        return normalized.contains("benh") && hasInfoKeyword;
    }
}