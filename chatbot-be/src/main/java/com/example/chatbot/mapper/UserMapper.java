package com.example.chatbot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.chatbot.dto.request.UserCreateRequest;
import com.example.chatbot.dto.request.UserUpdateRequest;
import com.example.chatbot.dto.response.UserResponse;
import com.example.chatbot.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    void toUpdate(@MappingTarget User user , UserUpdateRequest request);
}
