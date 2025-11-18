package com.example.bigfood.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.bigfood.dto.request.SearchRequest;
import com.example.bigfood.dto.response.ConversationResponse;
import com.example.bigfood.dto.response.MessageResponse;
import com.example.bigfood.dto.response.SymptomResponse;
import com.example.bigfood.dto.response.DiseaseResponse;
import com.example.bigfood.entity.Conversation;
import com.example.bigfood.entity.Message;
import com.example.bigfood.entity.User;
import com.example.bigfood.enums.ErrorCode;
import com.example.bigfood.exception.AppException;
import com.example.bigfood.mapper.ConversationMapper;
import com.example.bigfood.mapper.MessageMapper;
import com.example.bigfood.repository.ConversationRepository;
import com.example.bigfood.repository.MessageRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {

    ConversationRepository conversationRepository;
    MessageRepository messageRepository;
    ConversationMapper conversationMapper;
    UserService userService;
    MessageMapper messageMapper;

    ModelApiService modelApiService;

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

            return "Chào bạn, tôi là trợ lý chẩn đoán bệnh. Bạn đang có triệu chứng gì?";
        }

        // 2. Luật về Cảm ơn / Tạm biệt (Farewells)
        if (lowerCaseMsg.contains("cảm ơn") ||
                lowerCaseMsg.contains("tạm biệt") ||
                lowerCaseMsg.equals("bye")) {

            return "Cảm ơn bạn đã sử dụng dịch vụ. Chúc bạn mau khỏe!";
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

    public ConversationResponse addConversation(String user_id) {
        User user = userService.getUserById(user_id);

        Conversation conversation = new Conversation();
        conversation.setName("NULL");
        conversation.setUser(user);
        conversation.setCreatedAt(LocalDateTime.now());

        return conversationMapper.toResponse(conversationRepository.save(conversation));
    }

    public void deleteConversation(String id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FIND));

        conversationRepository.delete(conversation);
    }

    public List<ConversationResponse> getHistory(String id) {
        User user = userService.getUserById(id);

        List<Conversation> conversations = conversationRepository.findByUser(user);

        return conversationMapper.toListResponse(conversations);
    }

    private MessageResponse addMessage(String conversation_id, Message message) {
        Conversation conversation = conversationRepository.findById(conversation_id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FIND));

        // Message message = new Message();
        // message.setContent(request.getContent());
        // message.setCreatedAt(LocalDateTime.now());
        message.setConversation(conversation); // Set relationship từ phía sở hữu FK

        return messageMapper.toResponse(messageRepository.save(message));
    }

    public MessageResponse chat(String conversation_id, SearchRequest request) {
        Message message = new Message();
        SymptomResponse symptomResponse = modelApiService.extractSymptom(request);
        System.out.println(symptomResponse.getSymptoms());   

        // trả về câu trả lời đơn giản nếu không có triệu chứng được phát hiện
        if (symptomResponse.getSymptoms().isEmpty()) {
            message = Message.builder()
                    .content(handleSimpleChat(request.getContent()))
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            DiseaseResponse diseaseResponse = modelApiService.predictDisease(symptomResponse.getSymptoms());
            // DiseaseResponse diseaseResponse = modelApiService.predictDisease(symptomResponse.getSymptoms());
            message = Message.builder()
                    .content("Dựa trên các triệu chứng bạn cung cấp, có thể bạn đang mắc phải: "
                            + String.join(", ", diseaseResponse.getDisease()))
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        return addMessage(conversation_id, message);
    }

}