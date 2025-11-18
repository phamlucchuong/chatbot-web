package com.example.bigfood.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bigfood.entity.Message;

public interface MessageRepository extends JpaRepository<Message, String>{

    
}
