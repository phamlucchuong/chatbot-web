package com.example.chatbot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openmap.api.key}")
    private String openMapApiKey;

    @Bean
    public WebClient goongWebClient() {
        return WebClient.builder()
                .baseUrl("https://rsapi.goong.io")
                .build();
    }

    @Bean
    public WebClient overpassWebClient() {
        return WebClient.builder()
                .baseUrl("https://overpass-api.de")
                .build();
    }

}
