package com.example.chatbot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatbot.dto.request.SearchRequest;
import com.example.chatbot.dto.response.ApiResponse;
import com.example.chatbot.dto.response.ChatResponse;
import com.example.chatbot.dto.response.ConversationResponse;
import com.example.chatbot.dto.response.MessageResponse;
import com.example.chatbot.service.ConversationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    ConversationService searchService;

    @PostMapping
    public ApiResponse<ConversationResponse> createConversation(@AuthenticationPrincipal Jwt jwt, @RequestBody String name) {
        String user_id = jwt.getSubject();
        System.out.println(user_id);
        return ApiResponse.<ConversationResponse>builder()
            .results(searchService.addConversation(user_id, name))
            .message("Conversation created successfully.")
            .build();
    }

    @PatchMapping("{conv_id}")
    public ApiResponse<List<Void>> updateName(@PathVariable String conv_id, @RequestBody String name) {
        searchService.updateConvName(conv_id, name);
        return ApiResponse.<List<Void>>builder()
            .message("Conversation history retrieved successfully.")
            .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ConversationResponse>> getConversationHistory(@AuthenticationPrincipal Jwt jwt) {
        String id = jwt.getSubject();
        return ApiResponse.<List<ConversationResponse>>builder()
            .message("Conversation history retrieved successfully.")
            .results(searchService.getHistory(id))
            .build();
    }




    @DeleteMapping("/{conversation_id}")
    public ApiResponse<Void> deleteConversation(@PathVariable String conversation_id) {
        searchService.deleteConversation(conversation_id);
        return ApiResponse.<Void>builder()
            .message("Conversation deleted successfully.")
            .build();
    }
    
    
    @GetMapping("/{conversation_id}/messages")
    public ApiResponse<List<MessageResponse>> getMessages(@PathVariable String conversation_id) {
        return ApiResponse.<List<MessageResponse>>builder()
            .results(searchService.getMessages(conversation_id))
            .message("Messages retrieved successfully.")
            .build();
    }

    @PostMapping("/{conversation_id}/chat")
    public ApiResponse<ChatResponse> chat(@PathVariable String conversation_id, @RequestBody SearchRequest request) {
        return ApiResponse.<ChatResponse>builder()
            .results(searchService.chat(conversation_id, request))
            .message("Chat processed successfully.")
            .build();
    }
}
