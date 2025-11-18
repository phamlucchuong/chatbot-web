package com.example.bigfood.repository;



import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bigfood.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
