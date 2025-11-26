package com.example.bigfood.service.conversation.diagnosis;

import com.example.bigfood.dto.request.SearchRequest;
import com.example.bigfood.dto.response.PredictResponse;
import com.example.bigfood.dto.response.SymptomResponse;
import com.example.bigfood.entity.Disease;
import com.example.bigfood.repository.DiseaseRepository;
import com.example.bigfood.service.ModelApiService;
import com.example.bigfood.service.conversation.response.PromptTemplates;
import com.example.bigfood.service.conversation.response.ResponseGenerator;
import com.example.bigfood.service.conversation.session.SessionManager;
import com.example.bigfood.service.conversation.session.SessionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiagnosisEngine {
    
    private final ModelApiService modelApiService;
    private final DiseaseRepository diseaseRepository;
    private final SessionManager sessionManager;
    private final ResponseGenerator responseGenerator;
    
    private static final int MAX_DIAGNOSIS_ATTEMPTS = 5;
    private static final double CONFIDENCE_THRESHOLD = 0.85;
    
    /**
     * Xử lý mô tả triệu chứng và chẩn đoán
     */
    public String processSymptoms(String conversationId, String userMessage, SessionState state) {
        // 1. Trích xuất triệu chứng
        SymptomResponse symptomResponse = modelApiService.extractSymptom(
            SearchRequest.builder().content(userMessage).build()
        );
        
        List<String> newSymptoms = symptomResponse.getSymptoms();
        
        if (newSymptoms == null || newSymptoms.isEmpty()) {
            return handleVagueComplaint(userMessage);
        }
        
        // 2. Cập nhật context triệu chứng
        sessionManager.addSymptoms(conversationId, Set.copyOf(newSymptoms));
        Set<String> allSymptoms = sessionManager.getSymptomContext(conversationId);
        
        // 3. Dự đoán bệnh
        PredictResponse prediction = modelApiService.predictDisease(new ArrayList<>(allSymptoms));
        
        state.setDiagnosisAttempts(state.getDiagnosisAttempts() + 1);
        
        // 4. Xử lý kết quả
        if (prediction.getConfidence() >= CONFIDENCE_THRESHOLD) {
            return handleHighConfidence(conversationId, prediction, state);
        }
        
        if (state.getDiagnosisAttempts() >= MAX_DIAGNOSIS_ATTEMPTS) {
            return handleDiagnosisFailure(allSymptoms, state);
        }
        
        return handleLowConfidence(userMessage, prediction, allSymptoms, state);
    }
    
    /**
     * Xử lý khi độ tin cậy cao
     */
    private String handleHighConfidence(String conversationId, PredictResponse prediction, SessionState state) {
        Disease disease = diseaseRepository.findById(prediction.getDisease_id()).orElse(null);
        
        if (disease == null) {
            return "Rất tiếc, tôi không có đủ dữ liệu để chẩn đoán chính xác. " +
                   "Bạn nên đến cơ sở y tế để được khám kỹ hơn.";
        }
        
        state.setDiagnosed(true);
        state.setDiagnosedDiseaseId(disease.getId());
        state.setDiagnosedDiseaseName(disease.getName());
        state.getAskedTopics().clear();
        state.setDiagnosisAttempts(0);
        sessionManager.updateState(conversationId, state);
        
        return responseGenerator.generate(
            PromptTemplates.HIGH_CONFIDENCE_DIAGNOSIS,
            disease.getName(),
            prediction.getConfidence() * 100,
            buildDiseaseContext(disease)
        );
    }
    
    /**
     * Xử lý khi độ tin cậy thấp
     */
    private String handleLowConfidence(String userMessage, PredictResponse prediction,
                                      Set<String> currentSymptoms, SessionState state) {
        String symptomsStr = String.join(", ", currentSymptoms);
        
        return responseGenerator.generate(
            PromptTemplates.LOW_CONFIDENCE_DIAGNOSIS,
            userMessage,
            symptomsStr,
            prediction.getDisease_name(),
            prediction.getConfidence() * 100
        );
    }
    
    /**
     * Xử lý khi chẩn đoán thất bại
     */
    private String handleDiagnosisFailure(Set<String> currentSymptoms, SessionState state) {
        state.setDiagnosed(false);
        state.setDiagnosedDiseaseId(null);
        state.setDiagnosedDiseaseName(null);
        state.getAskedTopics().clear();
        state.setDiagnosisAttempts(0);
        
        String symptomsStr = currentSymptoms.isEmpty()
            ? "chưa có triệu chứng cụ thể nào được ghi nhận"
            : String.join(", ", currentSymptoms);
        
        return responseGenerator.generate(PromptTemplates.UNKNOWN_DIAGNOSIS, symptomsStr);
    }
    
    /**
     * Xử lý than phiền mơ hồ
     */
    private String handleVagueComplaint(String userMessage) {
        return responseGenerator.generate(PromptTemplates.VAGUE_COMPLAINT, userMessage);
    }
    
    private String buildDiseaseContext(Disease disease) {
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
}
