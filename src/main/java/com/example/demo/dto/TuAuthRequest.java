package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // 👈 Import ที่นี่

// *** นี่คือไฟล์ที่ถูกต้อง***
public class TuAuthRequest {

    @JsonProperty("UserName") // 👈 ใส่ที่นี่
    private String userName;

    @JsonProperty("PassWord") // 👈 ใส่ที่นี่
    private String passWord;

    // Constructor
    public TuAuthRequest(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }

    // Setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}