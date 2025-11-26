package com.example.bigfood.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bigfood.dto.request.SearchRequest;
import com.example.bigfood.dto.response.ChatResponse;
import com.example.bigfood.dto.response.ConversationResponse;
import com.example.bigfood.dto.response.MessageResponse;
import com.example.bigfood.dto.response.PredictResponse;
import com.example.bigfood.dto.response.RagResponse;
import com.example.bigfood.dto.response.SymptomResponse;
import com.example.bigfood.entity.Conversation;
import com.example.bigfood.entity.Disease;
import com.example.bigfood.entity.Message;
import com.example.bigfood.entity.User;
import com.example.bigfood.enums.ErrorCode;
import com.example.bigfood.exception.AppException;
import com.example.bigfood.mapper.ConversationMapper;
import com.example.bigfood.mapper.MessageMapper;
import com.example.bigfood.repository.ConversationRepository;
import com.example.bigfood.repository.DiseaseRepository;
import com.example.bigfood.repository.MessageRepository;

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
    DiseaseRepository diseaseRepository;
    ConversationMapper conversationMapper;
    UserService userService;
    MessageMapper messageMapper;
    ModelApiService modelApiService;

    static final Map<String, Set<String>> SYMPTOM_CONTEXT = new ConcurrentHashMap<>();
    static final Map<String, Integer> REQUEST_LIMIT = new ConcurrentHashMap<>(); // Giới hạn số triệu chứng trong ngữ cảnh

    /**
     * Xử lý các câu hỏi đơn giản, dựa trên luật (keywords).
     * 
     * @param message Tin nhắn của người dùng.
     * @return Câu trả lời nếu khớp luật, hoặc null nếu không khớp.
     */
    private String handleSimpleChat(String message) {
        String lowerCaseMsg = message.toLowerCase();

        // 1. Luật về Chào hỏi (Greetings)
        if (lowerCaseMsg.contains("chào") ||
                lowerCaseMsg.contains("hello") ||
                lowerCaseMsg.equals("hi")) {

            return "Chào bạn, tôi là Chat Skibidi - một trợ lý ảo chẩn đoán bệnh. Tôi có thể giúp được gì cho bạn?";
        }

        // 2. Luật về Cảm ơn / Tạm biệt (Farewells)
        if (lowerCaseMsg.contains("cảm ơn") ||
                lowerCaseMsg.contains("tạm biệt") ||
                lowerCaseMsg.equals("bye")) {

            return "Cảm ơn bạn đã sử dụng dịch vụ. Chúc bạn mau khỏe! Nếu có thắc mắc cần được giải đáp, hãy quay lại nhé!";
        }

        // 3. Luật về "Giúp đỡ"
        if (lowerCaseMsg.contains("giúp") || lowerCaseMsg.contains("làm gì")) {
            return "Bạn hãy mô tả các triệu chứng của mình (ví dụ: 'tôi bị sốt và đau đầu'), tôi sẽ cố gắng chẩn đoán giúp bạn.";
        }

        // ... (thêm các luật khác nếu muốn) ...

        // Nếu không khớp bất kỳ luật nào, trả về null
        // để báo cho luồng chính tiếp tục gọi AI.
        return null;
    }

    public ConversationResponse addConversation(String user_id, String name) {
        User user = userService.getUserById(user_id);

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
    }

    public List<ConversationResponse> getConversation(String id) {
        User user = userService.getUserById(id);

        List<Conversation> conversations = conversationRepository.findByUser(user);

        return conversationMapper.toListResponse(conversations);
    }

    @Async
    private void saveMessageAsync(String conversation_id, Message message) {
        try {
            Conversation conversation = conversationRepository.findById(conversation_id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            message.setConversation(conversation);
            messageRepository.save(message);
            log.info("Message saved asynchronously: {}", message.getId());
        } catch (Exception e) {
            log.error("Error saving message asynchronously: {}", e.getMessage());
        }
    }

    private String handlePredictDiseaseChat(Disease disease) {
        return "Dựa trên các triệu chứng bạn cung cấp, có thể bạn đang mắc phải: " + disease.getName()
                + "\n\nĐây là một căn bệnh phổ biến ở Việt Nam. " + disease.getDescription()
                + "\n\nCác triệu chứng chính bao gồm: " + disease.getSymptoms()
                + "\n\nNhững nguyên nhân dẫn đến bệnh: " + disease.getCauses()
                + "\n\nBiện pháp phòng ngừa: " + disease.getPreventions()
                + "\n\nVui lòng tham khảo ý kiến bác sĩ để được chẩn đoán chính xác và điều trị phù hợp.";

    }

    @Transactional
    public ChatResponse chat(String conversation_id, SearchRequest request) {
        // 1. Lưu tin nhắn của người dùng vào DB
        Conversation conversation = conversationRepository.findById(conversation_id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Message userMessage = Message.builder()
                .content(request.getContent())
                .bot(false)
                .createdAt(LocalDateTime.now())
                .conversation(conversation)
                .build();
        
        Message savedUserMessage = messageRepository.save(userMessage);
        MessageResponse userMessageResponse = messageMapper.toResponse(savedUserMessage);

        // 2. Xử lý AI và tạo response
        String botResponseContent;

        String content = handleSimpleChat(request.getContent());
        if (content != null) {
            botResponseContent = content;
        } else {
            // nhận dạng triệu chứng từ user
            SymptomResponse symptomResponse = modelApiService.extractSymptom(request);
            log.info("Extracted symptoms: {}", symptomResponse.getSymptoms());

            // xử lý nhớ ngữ cảnh triệu chứng
            List<String> symptoms = symptomResponse.getSymptoms();
            Set<String> contextSymptoms;
            if(SYMPTOM_CONTEXT.containsKey(conversation_id)) {
                contextSymptoms = SYMPTOM_CONTEXT.get(conversation_id);
                for (String symptom : symptoms) {
                    if (!contextSymptoms.contains(symptom)) {
                        contextSymptoms.add(symptom);
                    }
                }
            } else {
                contextSymptoms = new HashSet<>(symptoms);
                SYMPTOM_CONTEXT.put(conversation_id, contextSymptoms);
                REQUEST_LIMIT.put(conversation_id, 0);
            }


            // chẩn đoán bệnh từ các triệu chứng trong ngữ cảnh
            PredictResponse predictResponse = modelApiService.predictDisease(new ArrayList<>(contextSymptoms));

            if(predictResponse.getConfidence() < 0.85 && REQUEST_LIMIT.get(conversation_id) < 2) {
                botResponseContent = "À, tôi hiểu rồi. Từ các triệu chứng mà bạn đã cung cấp, rất có thể bạn đang mắc bệnh: " + predictResponse.getDisease_name()
                    + "\n\nNhưng để chắc chắn hơn, tôi cần thêm thông tin về các triệu chứng mà bạn gặp phải để có thể đưa ra chẩn đoán chính xác hơn. "
                    + "\n\nBạn có thể mô tả chi tiết hơn về tình trạng sức khỏe hiện tại của mình không?";

                REQUEST_LIMIT.put(conversation_id, REQUEST_LIMIT.get(conversation_id) + 1);
            } else {
                // Disease disease = diseaseRepository.findById(predictResponse.getDisease_id())
                //         .orElseThrow(() -> new AppException(ErrorCode.DISEASE_NOT_FOUND));
                // botResponseContent = handlePredictDiseaseChat(disease);
                RagResponse ragResponse = modelApiService.ragResponse(
                    com.example.bigfood.dto.request.RagRequest.builder()
                        .disease_id(predictResponse.getDisease_id())
                        .user_query(request.getContent())
                        .build()
                );

                botResponseContent = ragResponse.getResponse();
            }
        }

        // 3. Lưu response của bot vào DB
        Message botMessage = Message.builder()
                .content(botResponseContent)
                .bot(true)
                .createdAt(LocalDateTime.now())
                .conversation(conversation)
                .build();
        
        Message savedBotMessage = messageRepository.save(botMessage);
        MessageResponse botMessageResponse = messageMapper.toResponse(savedBotMessage);

        // 4. Trả về cả hai messages cho frontend
        return ChatResponse.builder()
                .userMessage(userMessageResponse)
                .botMessage(botMessageResponse)
                .build();
    }



    public List<MessageResponse> getMessages(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        return conversation.getMessages().stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public List<ConversationResponse> getHistory(String id) {
        User user = userService.getUserById(id);
        List<Conversation> conversations = conversationRepository.findByUser(user);
        return conversationMapper.toListResponse(conversations);
    }

    public void updateConvName(String conv_id, String name) {
        Conversation conversation = conversationRepository.findById(conv_id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        conversation.setName(name);
        conversationRepository.save(conversation);
    }

}