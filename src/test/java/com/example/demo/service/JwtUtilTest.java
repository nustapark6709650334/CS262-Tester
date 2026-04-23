package com.example.demo.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.service.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        //  จำลองการฉีดค่า @Value ลงในตัวแปร private 
        ReflectionTestUtils.setField(jwtUtil, "secret", "CstuCoursesExplorerSuperSecretKey2026!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationTimeMs", 3600000L); // 1 ชั่วโมง
    }

    @Test
    @DisplayName("Should generate token and extract correct username") // สร้าง Token ได้ และ ดึงค่า Username กลับมาได้
    void testGenerateAndExtractUsername() {
        String expectedUsername = "6500000000"; // รหัสนักศึกษา

        // Act
        String token = jwtUtil.generateToken(expectedUsername);
        String actualUsername = jwtUtil.extractUsername(token);

        // Assert
        assertThat(token).isNotNull();
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Test
    @DisplayName("Should return true when validating a valid token") // ตรวจ token
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken("student1");
        
        Boolean isValid = jwtUtil.validateToken(token);
        
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return null when extracting username from a malformed token") // คืนค่า null เมื่อ token ผิดรูปแบบ
    void testExtractUsername_InvalidToken() {
        String fakeToken = "this.is.a.fake.token";

        // คืนค่า null
        String result = jwtUtil.extractUsername(fakeToken);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle exceptions appropriately when token is expired") // เช็คว่า tokent หมดอายุยัง
    void testTokenExpiration() {
        // เปลี่ยนค่าเวลาหมดอายุให้ติดลบ ทำให้เวลาสร้างมันหมดอายุทันที
        ReflectionTestUtils.setField(jwtUtil, "expirationTimeMs", -1000L);
        String expiredToken = jwtUtil.generateToken("student1");

        // ทดสอบ extractUsername จับ Exception แล้ว ต้องได้ null
        String username = jwtUtil.extractUsername(expiredToken);
        assertThat(username).isNull();

        // Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.isTokenExpired(expiredToken);
        });
    }
}