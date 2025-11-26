package com.example.bigfood.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bigfood.service.MapboxService;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*") // Cho phép React gọi API
public class HospitalController {

    @Autowired
    private MapboxService mapboxService;

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearby(
            @RequestParam double lat,
            @RequestParam double lng) {
        
        // Validate input
        if (lat < -90 || lat > 90) {
            return ResponseEntity.badRequest()
                .body("Vĩ độ (latitude) phải nằm trong khoảng -90 đến 90");
        }
        if (lng < -180 || lng > 180) {
            return ResponseEntity.badRequest()
                .body("Kinh độ (longitude) phải nằm trong khoảng -180 đến 180");
        }
        
        // Gọi service xử lý
        List<MapboxService.HospitalDTO> hospitals = mapboxService.findNearbyHospitals(lat, lng);
        
        return ResponseEntity.ok(hospitals);
    }
}