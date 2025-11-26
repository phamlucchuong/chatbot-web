package com.example.bigfood.service;

import java.time.LocalDateTime;
import java. util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bigfood. dto.request.SearchRequest;
import com.example. bigfood.dto.response. ChatResponse;
import com.example. bigfood.dto.response.ConversationResponse;
import com.example.bigfood.dto.response.MessageResponse;
import com.example.bigfood. entity.Conversation;
import com.example. bigfood.entity.Message;
import com.example.bigfood.entity.User;
import com.example.bigfood. enums.ErrorCode;
import com.example. bigfood.exception.AppException;
import com.example. bigfood.mapper.ConversationMapper;
import com.example.bigfood. mapper.MessageMapper;
import com.example.bigfood.repository.ConversationRepository;
import com.example.bigfood. repository.MessageRepository;
import com.example.bigfood.service.conversation.diagnosis.DiagnosisEngine;
import com.example.bigfood.service.conversation.disease.DiseaseInfoHandler;
import com.example.bigfood.service.conversation.intent.IntentClassifier;
import com.example. bigfood.service.conversation. intent.UserIntent;
import com.example.bigfood.service.conversation.response. PromptTemplates;
import com.example.bigfood.service.conversation.response.ResponseGenerator;
import com.example.bigfood.service.conversation.session.SessionManager;
import com.example. bigfood.service.conversation. session.SessionState;
import com.example.bigfood.service.conversation.util.TextNormalizer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConversationService {

    ConversationRepository conversationRepository;
    MessageRepository messageRepository;
    ConversationMapper conversationMapper;
    MessageMapper messageMapper;
    UserService userService;
    
    SessionManager sessionManager;
    IntentClassifier intentClassifier;
    DiagnosisEngine diagnosisEngine;
    DiseaseInfoHandler diseaseInfoHandler;
    ResponseGenerator responseGenerator;
    TextNormalizer textNormalizer;

    private static final List<String> END_SESSION_KEYWORDS = List.of(
        "cảm ơn", "cảm ơn bạn", "tạm biệt", "bye", "ok", "oke", "được rồi", "xong rồi"
    );

    
    public ConversationResponse addConversation(String userId, String name) {
        User user = userService.getUserById(userId);
        Conversation conversation = new Conversation();
        conversation.setName(name);
        conversation.setUser(user);
        conversation.setCreatedAt(LocalDateTime.now());
        return conversationMapper.toResponse(conversationRepository.save(conversation));
    }

    public void deleteConversation(String id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        conversationRepository.delete(conversation);
        sessionManager.removeState(id);
    }

    public List<ConversationResponse> getConversation(String id) {
        User user = userService.getUserById(id);
        return conversationMapper.toListResponse(conversationRepository.findByUser(user));
    }

    public List<ConversationResponse> getHistory(String id) {
        User user = userService.getUserById(id);
        return conversationMapper.toListResponse(conversationRepository.findByUser(user));
    }

    public void updateConvName(String convId, String name) {
        Conversation conversation = conversationRepository.findById(convId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        conversation.setName(name);
        conversationRepository.save(conversation);
    }

    public List<MessageResponse> getMessages(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                . orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return conversation.getMessages().stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    
    @Transactional
    public ChatResponse chat(String conversationId, SearchRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Message userMessage = Message.builder()
                .content(request.getContent())
                .bot(false)
                .createdAt(LocalDateTime.now())
                .conversation(conversation)
                .build();
        Message savedUserMessage = messageRepository.save(userMessage);

        SessionState state = sessionManager.getOrCreateState(conversationId);
        String userContent = request.getContent() == null ? "" : request.getContent().trim();

        log.info("Chat received for conversation {}: {}", conversationId, userContent);

        // Kiểm tra closing phrase
        if (containsClosingPhrase(userContent)) {
            state.setSessionClosed(true);
            sessionManager.updateState(conversationId, state);
            log.info("Session closed for conversation {}", conversationId);
        }

        // Reset session nếu cần
        if (state.isSessionClosed() && looksLikeSymptoms(userContent)) {
            sessionManager.resetSession(conversationId);
            state = sessionManager.getOrCreateState(conversationId);
            log.info("Session reset for conversation {}", conversationId);
        }

        
        String declaredResponse = diseaseInfoHandler.handleDeclaredDisease(
            conversationId, userContent, state
        );
        
        String botResponseContent;
        
        if (declaredResponse != null) {
            log.info("Conversation {}: DECLARED_DISEASE, Diagnosed: {}", 
                     conversationId, state.isDiagnosed());
            botResponseContent = declaredResponse;
        } else {
            String directResponse = diseaseInfoHandler.handleDirectQuestion(
                conversationId, userContent, state
            );
            
            if (directResponse != null) {
                log.info("Conversation {}: DIRECT_DISEASE_INFO, Diagnosed: {}", 
                         conversationId, state. isDiagnosed());
                botResponseContent = directResponse;
            } else {
                botResponseContent = processUserMessage(conversationId, userContent, state);
            }
        }

        Message botMessage = Message.builder()
                .content(botResponseContent)
                .bot(true)
                .createdAt(LocalDateTime.now())
                .conversation(conversation)
                .build();
        Message savedBotMessage = messageRepository.save(botMessage);

        return ChatResponse.builder()
                . userMessage(messageMapper.toResponse(savedUserMessage))
                .botMessage(messageMapper.toResponse(savedBotMessage))
                .build();
    }
    
    private String processUserMessage(String conversationId, String userContent, SessionState state) {
        UserIntent intent = intentClassifier.classify(userContent, state);
        log.info("Conversation {}: Intent={}, Diagnosed={}", 
                 conversationId, intent, state.isDiagnosed());

        return handleIntent(conversationId, userContent, intent, state);
    }

    private String handleIntent(String conversationId, String userContent, 
                                UserIntent intent, SessionState state) {
        return switch (intent) {
            case GREETING -> handleGreeting(state);
            
            case FAREWELL, THANKS -> {
                state.setSessionClosed(true);
                sessionManager.updateState(conversationId, state);
                yield handleFarewell();
            }
            
            // OFF-TOPIC: Hỏi thứ không liên quan
            case OFF_TOPIC -> {
                log.debug("Handling OFF_TOPIC");
                yield handleOffTopic(userContent, state);
            }
            
            // VAGUE_COMPLAINT: Hỏi chung chung
            case VAGUE_COMPLAINT -> {
                log.debug("Handling VAGUE_COMPLAINT");
                yield responseGenerator.generate(
                    PromptTemplates.VAGUE_COMPLAINT, userContent
                );
            }
            
            // SYMPTOM_DESCRIPTION: Mô tả triệu chứng cụ thể
            case SYMPTOM_DESCRIPTION -> {
                log.debug("Handling SYMPTOM_DESCRIPTION");
                yield diagnosisEngine.processSymptoms(conversationId, userContent, state);
            }
            
            // ASK_DISEASE_INFO: Hỏi thông tin bệnh
            case ASK_DISEASE_INFO -> {
                log.debug("Handling ASK_DISEASE_INFO");
                yield handleDiseaseInfoQuestion(conversationId, userContent, state);
            }

            // ASK_MEDICAL_CONTEXT: Hỏi context (sau khi ra viện, chế độ ăn, etc.)
            case ASK_MEDICAL_CONTEXT -> {
                log.debug("Handling ASK_MEDICAL_CONTEXT");
                yield handleScopeLimitation(userContent);
            }
            
            // DECLARE_DISEASE: Tự khai báo bệnh
            case DECLARE_DISEASE -> {
                log.debug("Handling DECLARE_DISEASE");
                String response = diseaseInfoHandler.handleDeclaredDisease(
                    conversationId, userContent, state
                );
                yield response != null ? response : "Không tìm thấy thông tin bệnh bạn nhắc đến.";
            }
            
            // OTHER: Các trường hợp khác
            case OTHER -> {
                log.debug("Handling OTHER");
                if (state.isDiagnosed()) {
                    yield diseaseInfoHandler.handleTopicQuestion(
                        conversationId, userContent, state
                    );
                }
                else if(intentClassifier.getMedicalKeywordMatcher().calculateMedicalScore(userContent) > 0) {
                    yield "Tôi đã ghi nhận câu hỏi của bạn. Để tôi có thể tư vấn chính xác, bạn vui lòng:\n" +
                          "• Nêu rõ các **triệu chứng** cụ thể (sốt, ho, đau, nôn, v.v.) để tôi chẩn đoán, HOẶC\n" +
                          "• Nêu tên **bệnh cụ thể** để tôi cung cấp thông tin chi tiết, HOẶC\n" +
                          "• Khai báo bệnh của bạn nếu đã đi khám rồi.";
                } else {
                    yield handleOffTopic(userContent, state);
                }
            }

            default -> "Xin lỗi, tôi không hiểu ý bạn.  Bạn có thể nói rõ hơn được không?";
        };
    }


    private String handleGreeting(SessionState state) {
        // Luôn trả lời lời chào tươi - không dùng flag greeted
        return "Chào bạn! Tôi là ChatCare – trợ lý y tế ảo. Bạn đang cảm thấy thế nào hôm nay? Hãy chia sẻ với tôi nhé. ";
    }

    private String handleFarewell() {
        return "Rất vui được hỗ trợ bạn! Chúc bạn sớm khỏe mạnh. Nhớ đến cơ sở y tế nếu triệu chứng kéo dài nhé!";
    }

    private String handleOffTopic(String userMessage, SessionState state) {
        String contextNote = "";
        if (state.isDiagnosed()) {
            contextNote = " (Lúc nãy chúng ta xác định bạn mắc: " + state.getDiagnosedDiseaseName() + ")";
        }
        return responseGenerator.generate(
            PromptTemplates.OFF_TOPIC_REDIRECT,
            userMessage + contextNote
        );
    }
    
    private String handleDiseaseInfoQuestion(String conversationId, String userContent, SessionState state) {
        if (state.isDiagnosed()) {
            return diseaseInfoHandler.handleTopicQuestion(conversationId, userContent, state);
        }
        return responseGenerator.generate(PromptTemplates.DISEASE_INFO_REDIRECT, userContent);
    }
    
    private String handleScopeLimitation(String userMessage) {
        return responseGenerator.generate(
            PromptTemplates.SCOPE_LIMITATION_PROMPT, userMessage
        );
    }

    private boolean containsClosingPhrase(String message) {
        if (message == null) return false;
        String normalized = textNormalizer.normalize(message);
        return END_SESSION_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private boolean looksLikeSymptoms(String msg) {
        if (msg == null) return false;
        String normalized = textNormalizer.normalize(msg);
        
        if (normalized.contains(",") || normalized.contains("_") || normalized.contains(";")) {
            return true;
        }
        
        return normalized.matches(". *(đau|sốt|nôn|ói|mệt|phát ban|tiêu chảy|khó chịu|chóng mặt|đầy hơi|tiếng bụng soi).*");
    }
}