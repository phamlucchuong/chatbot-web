package com.example.bigfood.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bigfood.dto.request.SearchRequest;
import com.example.bigfood.dto.response.ApiResponse;
import com.example.bigfood.dto.response.ConversationResponse;
import com.example.bigfood.dto.response.MessageResponse;
import com.example.bigfood.service.ConversationService;
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
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    ConversationService searchService;

    @PostMapping
    public ApiResponse<ConversationResponse> createConversation(@AuthenticationPrincipal Jwt jwt) {
        String user_id = jwt.getSubject();
        System.out.println(user_id);
        return ApiResponse.<ConversationResponse>builder()
            .results(searchService.addConversation(user_id))
            .message("Search history logged successfully.")
            .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ConversationResponse>> getConversationHistory(@AuthenticationPrincipal Jwt jwt) {
        String id = jwt.getSubject();
        return ApiResponse.<List<ConversationResponse>>builder()
            .message("Search history retrieved successfully.")
            .results(searchService.getHistory(id))
            .build();
    }


    @DeleteMapping("/{conversation_id}")
    public ApiResponse<Void> deleteConversation(@PathVariable String conversation_id) {
        searchService.deleteConversation(conversation_id);
        return ApiResponse.<Void>builder()
            .message("Search history logged successfully.")
            .build();
    }
    
    
    // @PostMapping("/{conversation_id}/message")
    // public ApiResponse<MessageResponse> createMessage(@PathVariable String conversation_id, @RequestBody SearchRequest request) {
    //     System.out.println(request.getContent());
    //     return ApiResponse.<MessageResponse>builder()
    //         .results(searchService.addMessage(conversation_id, request))
    //         .message("Search history logged successfully.")
    //         .build();
    // }

    @PostMapping("/{conversation_id}/chat")
    public ApiResponse<MessageResponse> chating(@PathVariable String conversation_id, @RequestBody SearchRequest request) {
        return ApiResponse.<MessageResponse>builder()
            .results(searchService.chat(conversation_id, request))
            .build();
    }

    // @PostMapping("/extract-symptoms")
    // public ApiResponse<PredictDiseaseResponse> predictDisease(@RequestBody SymptonResponse request) {
    //     PredictDiseaseResponse prediction = predictDiseaseService.predictDisease(request.getContent());
    //     return ApiResponse.<PredictDiseaseResponse>builder()
    //         .results(prediction)
    //         .message("Disease prediction completed successfully.")
    //         .build();
    // }


    // @PostMapping("/predict-diusease")
    // public ApiResponse<PredictDiseaseResponse> predictDisease(@RequestBody SearchRequest request) {
    //     PredictDiseaseResponse prediction = predictDiseaseService.predictDisease(request.getContent());
    //     return ApiResponse.<PredictDiseaseResponse>builder()
    //         .results(prediction)
    //         .message("Disease prediction completed successfully.")
    //         .build();
    // }
}
