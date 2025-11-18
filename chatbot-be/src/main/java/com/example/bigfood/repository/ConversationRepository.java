package com.example.bigfood.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bigfood.entity.Conversation;
import com.example.bigfood.entity.User;


public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByUser(User user);
    
}
