package com.example.bigfood.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelApiGeminiService {

    private final WebClient client;
    private final String model;
    private final String apiKey;
    private final double defaultTemperature = 0.7;
    private final int defaultMaxTokens = 2048;
    private final String generatePath;

    /**
     * Khởi tạo Gemini API Client
     */
    public ModelApiGeminiService(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model) {

        this.model = model;
        this.apiKey = apiKey;

        boolean validKey = apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("${");

        if (!validKey) {
            log.error("Gemini API Key không hợp lệ hoặc bị thiếu!");
            throw new IllegalArgumentException("Gemini API Key is missing or invalid");
        }

        log.info("Initializing Gemini API Client...");
        log.info("Model: {}", model);
        log.info("API Key length: {}", apiKey.length());

        this.client = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1")  // Base URL chính thức của Gemini v1
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.generatePath = "/models/" + model + ":generateContent?key=" + apiKey;

        log.info("Gemini client initialized successfully!");
    }

    /**
     * Gửi prompt tới Gemini và nhận phản hồi
     */
    public String generate(String systemPrompt, String userPrompt) {
        return generate(PromptPayload.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .build());
    }

    /**
     * Gửi prompt tùy chỉnh tới Gemini bằng payload cấu hình.
     */
    public String generate(PromptPayload payload) {
        Objects.requireNonNull(payload, "Prompt payload must not be null");

        String fullPrompt = payload.systemPrompt == null || payload.systemPrompt.isBlank()
                ? payload.userPrompt
                : payload.systemPrompt + "\n\n" + payload.userPrompt;

        GeminiRequest requestBody = GeminiRequest.of(
                fullPrompt,
                payload.temperature != null ? payload.temperature : defaultTemperature,
                payload.maxTokens != null ? payload.maxTokens : defaultMaxTokens
        );

        try {
            log.info("📡 Sending request to Gemini model: {}", model);

            GeminiResponse resp = client.post()
                    .uri(generatePath)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (resp == null) {
                log.error("Gemini API returned NULL response");
                return "Không nhận được phản hồi từ Gemini.";
            }

            return extractGeminiResponse(resp);

        } catch (WebClientResponseException e) {
            log.error("HTTP Error from Gemini: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return handleApiError(e);

        } catch (Exception e) {
            log.error("Unexpected Error: {}", e.getMessage());
            return "Lỗi không xác định khi gọi Gemini: " + e.getMessage();
        }
    }

    /**
     * Test kết nối nhanh với Gemini
     */
    public boolean testConnection() {
        try {
            log.info("Testing Gemini connection...");

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", "Hello")))
                )
            );

            Map<?, ?> resp = client.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            boolean success = resp != null;
            log.info("Connection test result: {}", success ? "SUCCESS" : "FAILED");

            return success;

        } catch (Exception e) {
            log.error("Gemini test connection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Trích xuất text trả về từ Gemini API response
     */
    private String extractGeminiResponse(GeminiResponse resp) {
        List<Candidate> candidates = resp.candidates;
        if (candidates == null || candidates.isEmpty()) {
            log.error("No candidate found in response");
            return "Không trả về nội dung hợp lệ.";
        }

        Content content = candidates.get(0).content;
        if (content == null || content.parts == null || content.parts.isEmpty()) {
            return "Không có nội dung trong phản hồi.";
        }

        String text = content.parts.get(0).text;
        return text != null ? text.trim() : "Chat không trả về nội dung text.";
    }

    /**
     * Xử lý lỗi HTTP từ Gemini API
     */
    private String handleApiError(WebClientResponseException e) {

        int code = e.getStatusCode().value();

        return switch (code) {
            case 400 -> "Lỗi 400 - Bad Request: " + e.getResponseBodyAsString();
            case 401, 403 -> "Lỗi xác thực.";
            case 404 -> "Model không tồn tại: " + model;
            case 429 -> "Quá nhiều request! Vui lòng thử lại sau.";
            case 500 -> "Lỗi sự cố (500).";
            default -> "Lỗi API: " + code + " - " + e.getMessage();
        };
    }

    public String getModelName() {
        return model;
    }

    public String getApiKeyStatus() {
        if (apiKey == null || apiKey.isBlank()) return "MISSING";
        if (apiKey.startsWith("${")) return "NOT_CONFIGURED";
        return "PRESENT";
    }

    /**
     * Payload cấu hình cho từng request tới Gemini
     */
    public static class PromptPayload {
        private final String systemPrompt;
        private final String userPrompt;
        private final Double temperature;
        private final Integer maxTokens;

        private PromptPayload(Builder builder) {
            this.systemPrompt = builder.systemPrompt;
            this.userPrompt = Objects.requireNonNull(builder.userPrompt, "User prompt must not be null or blank");
            this.temperature = builder.temperature;
            this.maxTokens = builder.maxTokens;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String systemPrompt;
            private String userPrompt;
            private Double temperature;
            private Integer maxTokens;

            public Builder systemPrompt(String systemPrompt) {
                this.systemPrompt = systemPrompt;
                return this;
            }

            public Builder userPrompt(String userPrompt) {
                this.userPrompt = userPrompt;
                return this;
            }

            public Builder temperature(Double temperature) {
                this.temperature = temperature;
                return this;
            }

            public Builder maxTokens(Integer maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }

            public PromptPayload build() {
                if (userPrompt == null || userPrompt.isBlank()) {
                    throw new IllegalArgumentException("User prompt is required");
                }
                return new PromptPayload(this);
            }
        }
    }

    private record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
        static GeminiRequest of(String prompt, double temperature, int maxTokens) {
            return new GeminiRequest(
                    List.of(new Content(List.of(new Part(prompt)))),
                    new GenerationConfig(temperature, maxTokens)
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GenerationConfig(double temperature, int maxOutputTokens) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Candidate(Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Part(String text) {}
}
