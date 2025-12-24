package com.example.chatbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.chatbot.entity.User;
import com.example.chatbot.enums.ErrorCode;
import com.example.chatbot.exception.AppException;
import com.example.chatbot.mapper.UserMapper;
import com.example.chatbot.repository.UserRepository;
import com.example.chatbot.dto.request.UserCreateRequest;
import com.example.chatbot.dto.response.UserResponse;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public Boolean verifyEmail(String emailRequest) {
        return userRepository.existsByEmail(emailRequest);
    }

    protected User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

}
