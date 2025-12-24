
package com.example.chatbot.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import com.example.chatbot.dto.response.ConversationResponse;
import com.example.chatbot.entity.Conversation;


@Mapper(componentModel = "spring")
public interface ConversationMapper {
    List<ConversationResponse> toListResponse(List<Conversation> historySearch);

    ConversationResponse toResponse(Conversation conversation);
}
