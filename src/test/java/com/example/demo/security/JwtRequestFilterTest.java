package com.example.demo.security;

import com.example.demo.service.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil; // จำลองคลาสที่ใช้จัดการ Token

    @Mock
    private HttpServletRequest request; // จำลอง Request ที่รับมาจากผู้ใช้

    @Mock
    private HttpServletResponse response; // จำลอง Response

    @Mock
    private FilterChain filterChain; // จำลองโซ่ของ Filter ตัวถัดไป

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    // เคลียร์ Context เสมอก่อนและหลังเทสต์ เพื่อไม่ให้ User Login ค้างไปเทสต์ถัดไป
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set user in SecurityContext when valid token is provided") // token ถูกแล้ว set user เข้า security context
    void testDoFilterInternal_WithValidToken() throws ServletException, IOException {
        // 1. Arrange: จำลองสถานการณ์ว่าส่ง Header มาถูกต้อง
        String token = "valid.jwt.token";
        String username = "student123";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // 2. Act: สั่งให้ Filter ทำงาน
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // 3. Assert: ตรวจสอบผลลัพธ์
        // ระบบต้องรู้ว่าใคร Login อยู่
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
        
        // ต้องส่งงานให้ Filter ตัวต่อไปทำด้วย
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should proceed without setting user when no Authorization header exists") // ถ้าไม่มี Authorization จะไม่ set user เข้า security context
    void testDoFilterInternal_NoHeader() throws ServletException, IOException {
        // 1. Arrange: ไม่มีการส่ง Header มาเลย
        when(request.getHeader("Authorization")).thenReturn(null);

        // 2. Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // 3. Assert
        // ต้องไม่มี User Login ในระบบ
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // แต่ระบบยังต้องให้ Request วิ่งต่อไป 
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should proceed without setting user when token is expired") // Token อายุ ไม่ set user เข้า security context แล้วแจ้ง ERROR500(ไม่สามารถประมวลผลคำขอจากเบราว์เซอร์ได้)
    void testDoFilterInternal_ExpiredToken() throws ServletException, IOException {
        // 1. Arrange: ส่ง Token มาแต่หมดอายุ
        String token = "expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // สั่งให้ Mock โยน Exception เมื่อเจอ Token นี้
        when(jwtUtil.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // 2. Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // 3. Assert
        // ต้องจับ Exception ได้ ไม่พัง และไม่มี User ใน Context
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}