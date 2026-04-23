package com.example.demo.dto;

// *** ไม่ต้อง import @JsonProperty ***

public class LoginRequest {

    private String username; // 👈 กลับไปใช้ตัวพิมพ์เล็ก
    private String password; // 👈 กลับไปใช้ตัวพิมพ์เล็ก

    // Constructor (ถ้ามีก็ดี)
    public LoginRequest() {}
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters (สำคัญมาก)
    public String getUsername() { // 👈 กลับไปใช้ getUsername (ตัว 'u' เล็ก)
        return username;
    }

    public String getPassword() { // 👈 กลับไปใช้ getPassword (ตัว 'p' เล็ก)
        return password;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}