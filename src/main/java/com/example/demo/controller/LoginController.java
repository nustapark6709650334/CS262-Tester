package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.TuAuthResponse;
import com.example.demo.service.JwtUtil;
import com.example.demo.service.TuAuthService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // อนุญาตให้ Frontend เรียกมา
public class LoginController {

    private final TuAuthService tuAuthService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LoginController(TuAuthService tuAuthService, JwtUtil jwtUtil) {
        this.tuAuthService = tuAuthService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        
        // 1. ส่งไปตรวจสอบกับ TU API
        TuAuthResponse tuResponse = tuAuthService.authenticate(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        );

        // 2. ถ้า TU API บอกว่าสำเร็จ
        if (tuResponse.isStatus()) {
            
            // 3. สร้าง JWT Token ของเราเอง
            final String token = jwtUtil.generateToken(loginRequest.getUsername());

            // 4. ส่ง Token กลับไปให้ Frontend
            LoginResponse successResponse = new LoginResponse(
                "Login สำเร็จ",
                tuResponse.getDisplayNameTh(),
                tuResponse.getEmail(),
                token, // ส่ง Token กลับไป
                tuResponse.getUsername()
            );
            return ResponseEntity.ok(successResponse);
        } else {
            // 5. ถ้าล้มเหลว
            LoginResponse errorResponse = new LoginResponse(
                tuResponse.getMessage() != null ? tuResponse.getMessage() : "Username หรือ Password ไม่ถูกต้อง",
                null, null, null/* ไม่มี Token*/,null 
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
 
}   

