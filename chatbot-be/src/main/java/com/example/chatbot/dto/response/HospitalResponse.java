package com.example.chatbot.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HospitalResponse {
    private double lat;
    private double lng;
    private String name;
}
