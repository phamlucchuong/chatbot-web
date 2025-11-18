
package com.example.bigfood.mapper;

import org.mapstruct.Mapper;
import com.example.bigfood.dto.response.MessageResponse;
import com.example.bigfood.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageResponse toResponse(Message message);
}
