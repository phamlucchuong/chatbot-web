package com.example.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = "com.example.chatbot")
@EnableAsync
public class BigfoodBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigfoodBeApplication.class, args);
	}

}
