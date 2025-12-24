package com.example.chatbot.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.chatbot.dto.response.ApiResponse;
import com.example.chatbot.dto.response.HospitalResponse;
import com.example.chatbot.dto.response.OverpassResponse;
import com.example.chatbot.service.MapService;


@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*") // Cho phép React gọi API
public class HospitalController {

    @Autowired
    private MapService mapService;

    @GetMapping("/nearby")
    public ApiResponse<List<HospitalResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") int radius) {
        
        return ApiResponse.<List<HospitalResponse>>builder()
                .results(mapService.findNearbyHospitals(lat, lng, radius))
                .build();
    }
}