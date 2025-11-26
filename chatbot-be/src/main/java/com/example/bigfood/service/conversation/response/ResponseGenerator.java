package com.example.bigfood.service.conversation.response;

import com.example.bigfood.service.ModelApiGeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseGenerator {
    
    private final ModelApiGeminiService geminiService;
    
    public String generate(String promptTemplate, Object... args) {
        String userPrompt = String.format(promptTemplate, args);
        
        return geminiService.generate(
            ModelApiGeminiService.PromptPayload.builder()
                .systemPrompt(PromptTemplates.SYSTEM_PROMPT)
                .userPrompt(userPrompt)
                .build()
        );
    }
}