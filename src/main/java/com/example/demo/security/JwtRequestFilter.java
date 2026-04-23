package com.example.demo.security;

import com.example.demo.service.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList; // ใช้สำหรับ Roles ว่างๆ

@Component //เพื่อให้ SecurityConfig @Autowire ได้
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // ตรวจสอบ Header และดึง Token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        }

        // ถ้าได้ Token มาและถูกต้อง
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ตรวจสอบความถูกต้อง (เช่น ยังไม่หมดอายุ)
            if (jwtUtil.validateToken(jwt)) {

                // สร้าง Authentication Token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, new ArrayList<>()); // (username, credentials, authorities)
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // บันทึกเข้าระบบ Security
                // Spring Security จะรับรู้ว่า User นี้ Login แล้วสำหรับ Request นี้
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // ส่งต่อไปยัง Filter ตัวถัดไป
        chain.doFilter(request, response);
    }
}