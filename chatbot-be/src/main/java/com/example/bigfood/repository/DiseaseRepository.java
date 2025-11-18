package com.example.bigfood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bigfood.entity.Disease;


public interface DiseaseRepository extends JpaRepository<Disease, String> {

}
