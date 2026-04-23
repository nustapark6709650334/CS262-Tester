package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.TuAuthResponse;
import com.example.demo.repository.CourseRepositoryN;
import com.example.demo.repository.CourseRepositoryS;
import com.example.demo.service.JwtUtil;
import com.example.demo.service.TuAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test - Person 1: Authentication & Access Integration
 *
 * Scope:
 *   Frontend login flow <-> /api/auth/login <-> TuAuthService (TU API)
 *                       <-> JwtUtil token issue
 *                       <-> JwtRequestFilter token validation
 *                       <-> SecurityConfig route protection
 *
 * The external TU API is mocked via @MockBean on TuAuthService so that these
 * tests focus on the *integration* of controller + security filter + token
 * layer, not the external dependency (that scope belongs to Person 3).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @MockBean
    private TuAuthService tuAuthService;

    @MockBean
    private CourseRepositoryN courseRepositoryN;

    @MockBean
    private CourseRepositoryS courseRepositoryS;

    // ---------- helpers ----------

    private TuAuthResponse tuSuccess(String username) {
        TuAuthResponse r = new TuAuthResponse();
        r.setStatus(true);
        r.setUsername(username);
        r.setDisplayNameTh("นักศึกษา ทดสอบ");
        r.setEmail(username + "@student.tu.ac.th");
        r.setType("student");
        return r;
    }

    private TuAuthResponse tuFailure(String message) {
        TuAuthResponse r = new TuAuthResponse();
        r.setStatus(false);
        r.setMessage(message);
        return r;
    }

    private String buildExpiredToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(username)
                .setIssuedAt(new Date(now - 120_000))
                .setExpiration(new Date(now - 1_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // =====================================================================
    // INT-AUTH-01: Login success with valid credential
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-01 Login success with valid credential issues JWT token")
    void intAuth01_loginSuccess_issuesToken() throws Exception {
        when(tuAuthService.authenticate(eq("57090001"), eq("CorrectPass!")))
                .thenReturn(tuSuccess("57090001"));

        LoginRequest req = new LoginRequest("57090001", "CorrectPass!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.username").value("57090001"))
                .andExpect(jsonPath("$.message").value("Login สำเร็จ"))
                .andExpect(jsonPath("$.email").value("57090001@student.tu.ac.th"));
    }

    // =====================================================================
    // INT-AUTH-02: Login fail with invalid password
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-02 Login fail with invalid password returns 401 and no token")
    void intAuth02_loginFail_invalidPassword() throws Exception {
        when(tuAuthService.authenticate(eq("57090001"), eq("WrongPass!")))
                .thenReturn(tuFailure("Invalid username or password"));

        LoginRequest req = new LoginRequest("57090001", "WrongPass!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    // =====================================================================
    // INT-AUTH-03: Login fail with missing username or password
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-03 Login with missing username/password does not issue token")
    void intAuth03_loginFail_missingInput() throws Exception {
        // Backend receives nulls for both fields; TU API call is simulated to fail.
        when(tuAuthService.authenticate(isNull(), isNull()))
                .thenReturn(tuFailure("Missing credentials"));

        // Empty JSON body -> username and password are both null
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    // =====================================================================
    // INT-AUTH-04: Protected page access without token
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-04 Protected endpoint without token is rejected")
    void intAuth04_protectedWithoutToken() throws Exception {
        when(courseRepositoryN.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/coursesN"))
                .andExpect(status().is4xxClientError()); // 401/403 depending on default entry point
    }

    // =====================================================================
    // INT-AUTH-05: Protected page access with invalid token
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-05 Protected endpoint with malformed token is rejected")
    void intAuth05_protectedWithInvalidToken() throws Exception {
        String garbageToken = "this.is.not-a-valid-jwt";

        mockMvc.perform(get("/api/coursesN")
                        .header("Authorization", "Bearer " + garbageToken))
                .andExpect(status().is4xxClientError());
    }

    // =====================================================================
    // INT-AUTH-06: Session expired handling
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-06 Protected endpoint with expired token is rejected")
    void intAuth06_expiredToken() throws Exception {
        String expired = buildExpiredToken("57090001");

        mockMvc.perform(get("/api/coursesN")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().is4xxClientError());
    }

    // =====================================================================
    // INT-AUTH-07: Logout flow and post-logout access control
    //
    // Logout is handled client-side by removing the token from localStorage
    // (see login.js). The backend contract under test here is that once the
    // token is no longer sent, protected routes MUST remain inaccessible.
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-07 After logout (token no longer sent), protected route stays locked")
    void intAuth07_logoutThenNoAccess() throws Exception {
        // 1. Obtain a valid token via login.
        when(tuAuthService.authenticate(eq("57090001"), eq("CorrectPass!")))
                .thenReturn(tuSuccess("57090001"));
        LoginRequest req = new LoginRequest("57090001", "CorrectPass!");
        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginBody).get("token").asText();

        // 2. Sanity check: token works.
        when(courseRepositoryN.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/coursesN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Simulate logout: client drops the token and then retries without it.
        mockMvc.perform(get("/api/coursesN"))
                .andExpect(status().is4xxClientError());
    }

    // =====================================================================
    // INT-AUTH-08: Role/program-based redirection after login
    //
    // Program routing is decided by login.js from substring(4,6) of username:
    //   "65" -> main.html      (ภาคพิเศษ / special program)
    //   "61" -> mainNormal.html (ภาคปกติ / normal program)
    // Backend contract: the login response MUST carry the username unchanged
    // so the frontend can route correctly.
    // =====================================================================
    @Test
    @DisplayName("INT-AUTH-08 Login response returns username that drives program redirection")
    void intAuth08_programBasedRedirection() throws Exception {
        // Special program (65)
        when(tuAuthService.authenticate(eq("65090001"), anyString()))
                .thenReturn(tuSuccess("65090001"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("65090001", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("65090001"));

        // Normal program (61)
        when(tuAuthService.authenticate(eq("61090002"), anyString()))
                .thenReturn(tuSuccess("61090002"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("61090002", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("61090002"));
    }
}
