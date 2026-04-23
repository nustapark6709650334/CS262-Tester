package com.example.demo.dto;

public class LoginResponse {
    private String message;
    private String displayName;
    private String email;
    private String token; // Token ที่จะส่งกลับไป
    private String username;

    public LoginResponse(String message, String displayName, String email, String token, String username) {
        this.message = message;
        this.displayName = displayName;
        this.email = email;
        this.token = token;
        this.username = username;
    }
    // Getters and Setters
    public String getMessage() { return message; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
}