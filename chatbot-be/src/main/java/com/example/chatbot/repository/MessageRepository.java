package com.example.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chatbot.entity.Message;

public interface MessageRepository extends JpaRepository<Message, String>{

    
}
