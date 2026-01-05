package com.example.chatbot.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chatbot.dto.request.SearchRequest;
import com.example.chatbot.dto.response.ChatResponse;
import com.example.chatbot.dto.response.ConversationResponse;
import com.example.chatbot.dto.response.MessageResponse;
import com.example.chatbot.dto.response.PredictResponse;
import com.example.chatbot.dto.response.RagResponse;
import com.example.chatbot.dto.response.SymptomResponse;
import com.example.chatbot.entity.Conversation;
import com.example.chatbot.entity.Message;
import com.example.chatbot.entity.User;
import com.example.chatbot.enums.ErrorCode;
import com.example.chatbot.exception.AppException;
import com.example.chatbot.mapper.ConversationMapper;
import com.example.chatbot.mapper.MessageMapper;
import com.example.chatbot.repository.ConversationRepository;
import com.example.chatbot.repository.MessageRepository;

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
    UserService userService;
    MessageMapper messageMapper;
    ModelApiService modelApiService;

    static final Map<String, Set<String>> SYMPTOM_CONTEXT = new ConcurrentHashMap<>();
    static final Map<String, Integer> REQUEST_LIMIT = new ConcurrentHashMap<>(); // Giới hạn số triệu chứng trong ngữ
                                                                                 // cảnh
    static String CURRENT_DISEASE_ID = "";
    Random random = new Random();

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

    private String genCallBackResponse(String diseaseName) {
        List<String> responses = List.of(
                // Câu 1: Thân thiện, gợi mở
                "Tôi đã tiếp nhận các triệu chứng bạn vừa nêu. Rất có thể tình trạng này liên quan đến bệnh: "
                        + diseaseName + ". "
                        + "Tuy nhiên, để có cái nhìn chính xác nhất, bạn có thể chia sẻ thêm những thay đổi khác trong cơ thể gần đây không?",

                // Câu 2: Chuyên nghiệp, thận trọng
                "Dựa trên phân tích ban đầu, các dấu hiệu của bạn có nhiều nét tương đồng với bệnh: " + diseaseName
                        + ". "
                        + "Để chẩn đoán được khách quan hơn, bạn vui lòng mô tả chi tiết hơn về tần suất và cường độ của các triệu chứng nhé.",

                // Câu 3: Đồng cảm, nhẹ nhàng
                "Tôi hiểu những khó chịu mà bạn đang trải qua. Những biểu hiện đó khá giống với bệnh: " + diseaseName
                        + ". "
                        + "Bạn hãy kể kỹ hơn một chút về tình trạng hiện tại của mình để tôi có thêm cơ sở hỗ trợ bạn tốt hơn nhé.",

                // Câu 4: Ngắn gọn, súc tích
                "Có khả năng bạn đang gặp vấn đề về: " + diseaseName + ". "
                        + "Để chắc chắn hơn, tôi cần thêm thông tin. Bạn có đang gặp thêm bất kỳ triệu chứng lạ nào khác không?",

                // Câu 5: Mang tính tư vấn
                "Hệ thống ghi nhận các triệu chứng của bạn rất giống với bệnh: " + diseaseName + ". "
                        + "Nhưng đây mới chỉ là dự đoán ban đầu. Bạn có thể cho tôi biết các triệu chứng này đã kéo dài bao lâu rồi không?",

                // Câu 6: Khuyến khích người dùng chia sẻ
                "Từ những thông tin bạn cung cấp, tôi nghi ngờ bạn đang mắc: " + diseaseName + ". "
                        + "Để hỗ trợ bạn tốt nhất, tôi rất cần bạn mô tả chi tiết hơn về cảm giác của mình lúc này.",

                // Câu 7: Theo hướng loại trừ
                "Các biểu hiện bạn kể hướng đến bệnh: " + diseaseName + ". "
                        + "Tuy nhiên, một số bệnh khác cũng có dấu hiệu tương tự. Bạn hãy cung cấp thêm thông tin để tôi giúp bạn phân biệt rõ hơn nhé.",

                // Câu 8: Chu đáo
                "Cảm ơn bạn đã chia sẻ. Theo dữ liệu hiện có, rất có thể bạn bị: " + diseaseName + ". "
                        + "Để tăng độ chính xác cho chẩn đoán, bạn hãy giúp tôi liệt kê chi tiết hơn tình trạng sức khỏe hiện tại của mình.",

                // Câu 9: Trực diện
                "Rất có khả năng bạn đang mắc bệnh: " + diseaseName + ". "
                        + "Dù vậy, thông tin hiện tại vẫn còn hơi ít. Bạn có thể nói rõ hơn về các triệu chứng đi kèm mà bạn nhận thấy không?",

                // Câu 10: Giọng điệu của một trợ lý y tế
                "Tôi đã phân tích các dấu hiệu bạn nêu và thấy chúng khá khớp với bệnh: " + diseaseName + ". "
                        + "Để đưa ra lời khuyên chính xác nhất, tôi cần bạn mô tả thêm về tình hình sức khỏe của bạn trong vài ngày qua.");
        return responses.get(random.nextInt(responses.size()));
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
            if (CURRENT_DISEASE_ID != "") {
                log.info("Existing context symptoms: " + SYMPTOM_CONTEXT.get(conversation_id));
                RagResponse ragResponse = modelApiService.ragResponse(
                        com.example.chatbot.dto.request.RagRequest.builder()
                                .disease_id(CURRENT_DISEASE_ID)
                                .user_query(request.getContent())
                                .build());

                botResponseContent = ragResponse.getResponse();
            } else {
                botResponseContent = content;
            }
        } else {
            // nhận dạng triệu chứng từ user
            SymptomResponse symptomResponse = modelApiService.extractSymptom(request);
            log.info("Extracted symptoms: {}", symptomResponse.getSymptoms());

            // xử lý nhớ ngữ cảnh triệu chứng
            List<String> symptoms = symptomResponse.getSymptoms();
            Set<String> contextSymptoms;
            if (SYMPTOM_CONTEXT.containsKey(conversation_id)) {
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
            log.info("Current context symptoms: " + contextSymptoms);

            // chẩn đoán bệnh từ các triệu chứng trong ngữ cảnh
            PredictResponse predictResponse = modelApiService.predictDisease(new ArrayList<>(contextSymptoms));

            if (predictResponse.getConfidence() < 0.65 && REQUEST_LIMIT.get(conversation_id) < 2) {
                botResponseContent = genCallBackResponse(predictResponse.getDisease_name());
                REQUEST_LIMIT.put(conversation_id, REQUEST_LIMIT.get(conversation_id) + 1);
            } else {
                CURRENT_DISEASE_ID = predictResponse.getDisease_id();
                RagResponse ragResponse = modelApiService.ragResponse(
                        com.example.chatbot.dto.request.RagRequest.builder()
                                .disease_id(predictResponse.getDisease_id())
                                .user_query(request.getContent())
                                .build());

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