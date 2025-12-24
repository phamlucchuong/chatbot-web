package com.example.chatbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chatbot.entity.Conversation;
import com.example.chatbot.entity.User;


public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByUser(User user);
    
}
