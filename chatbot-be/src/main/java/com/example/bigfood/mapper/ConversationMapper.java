
package com.example.bigfood.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import com.example.bigfood.dto.response.ConversationResponse;
import com.example.bigfood.entity.Conversation;


@Mapper(componentModel = "spring")
public interface ConversationMapper {
    List<ConversationResponse> toListResponse(List<Conversation> historySearch);

    ConversationResponse toResponse(Conversation conversation);
}
