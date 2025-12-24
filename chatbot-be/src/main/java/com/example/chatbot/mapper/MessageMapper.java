
package com.example.chatbot.mapper;

import org.mapstruct.Mapper;
import com.example.chatbot.dto.response.MessageResponse;
import com.example.chatbot.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageResponse toResponse(Message message);
}
