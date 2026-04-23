package com.example.demo.config;

import com.example.demo.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. ปิด CSRF (จำเป็นสำหรับ Token-based)
            .csrf(csrf -> csrf.disable()) 
            .cors(cors -> {}) // (อนุญาต CORS ถ้าตั้งค่าไว้)

            // 2. กำหนดสิทธิ์การเข้าถึง
            .authorizeHttpRequests(authz -> authz
                
                // อนุญาตให้ทุกคนเข้าถึง API สำหรับ Login
                .requestMatchers("/api/auth/login").permitAll() // <--- ใช้ Path จาก JS ของคุณ

                // อนุญาตให้ทุกคนเข้าถึงไฟล์ Frontend
                .requestMatchers(
                        "/",
                        "/styles.css",
                        "/photo/**",
                        "/*.html",  
                        "/*.js", 
                        "/*.css",
                        "/favicon.ico",
                        "/assets/**"   
                ).permitAll()

                // 3. (เพิ่ม) อนุญาต API ให้คนที่มี Token เข้าถึงได้ 
                .requestMatchers("/api/coursesS/**", "/api/coursesN/**").authenticated()
                
                // Path อื่นๆ ทั้งหมด (เช่น /api/courses) ต้องยืนยันตัวตน
                .anyRequest().authenticated() 
            )
            
            // 3. ตั้งค่า Session เป็น STATELESS (เพราะเราใช้ Token)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            );

        // 4. เพิ่ม Filter ของเรา
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}