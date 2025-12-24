package com.example.chatbot.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Element {
    private Center center;
    private Map<String, String> tags;
}