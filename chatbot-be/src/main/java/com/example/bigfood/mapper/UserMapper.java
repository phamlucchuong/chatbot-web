package com.example.bigfood.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.example.bigfood.dto.request.UserCreateRequest;
import com.example.bigfood.dto.request.UserUpdateRequest;
import com.example.bigfood.dto.response.UserResponse;
import com.example.bigfood.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
    
    void toUpdate(@MappingTarget User user , UserUpdateRequest request);
}
