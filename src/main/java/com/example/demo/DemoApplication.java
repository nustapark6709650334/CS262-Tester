package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // 1. Import
import org.springframework.web.client.RestTemplate; // 2. Import

@SpringBootApplication
public class DemoApplication { // หรือชื่อไฟล์หลักของคุณ

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}