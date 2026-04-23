package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TuAuthResponse {
    private boolean status;
    private String message;
    @JsonProperty("displayname_th")
    private String displayNameTh;
    private String email;
    private String type;
    private String username;
    // Getters and Setters
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDisplayNameTh() { return displayNameTh; }
    public void setDisplayNameTh(String displayNameTh) { this.displayNameTh = displayNameTh; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}