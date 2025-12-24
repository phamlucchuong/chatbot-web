package com.example.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chatbot.entity.Disease;


public interface DiseaseRepository extends JpaRepository<Disease, String> {

}
