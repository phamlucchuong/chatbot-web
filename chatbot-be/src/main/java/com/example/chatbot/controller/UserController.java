package com.example.chatbot.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.chatbot.dto.request.UserCreateRequest;
import com.example.chatbot.dto.response.UserResponse;
import com.example.chatbot.dto.response.ApiResponse;
import com.example.chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    
    @GetMapping("/verify-email/{email}")
    public ApiResponse<Boolean> verifyEmail(@PathVariable String email){
        return ApiResponse.<Boolean>builder()
        .results(userService.verifyEmail(email))
        .build();
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@RequestBody UserCreateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResults(userService.createUser(request));
        return apiResponse;
    }
}
