package com.example.chatbot.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

public class OpenMapResponse {
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NearbyResponse {
        private List<Feature> features;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String name;
        private double lat;
        private double lon;
    }

}
