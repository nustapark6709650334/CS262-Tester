package com.example.demo.service;

import com.example.demo.dto.TuAuthRequest;
import com.example.demo.dto.TuAuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TuAuthServiceTest {

    @Mock
    private RestTemplate restTemplate; // จำลองตัวยิง API

    @InjectMocks
    private TuAuthService tuAuthService;
    
    private final String dummyApiUrl = "https://api.tu.ac.th/auth";
    private final String dummyApiKey = "dummy-api-key";

    @BeforeEach
    void setUp() {
        // ฉีดค่า @Value เข้าไปใน Service
        ReflectionTestUtils.setField(tuAuthService, "tuApiUrl", dummyApiUrl);
        ReflectionTestUtils.setField(tuAuthService, "tuApiKey", dummyApiKey);
    }

    @Test
    @DisplayName("Should return success response when API call is successful") // ยิง API สำเร็จ คืนค่า Response 200 OK
    void testAuthenticate_Success() {
        // 1. Arrange: เตรียมข้อมูลจำลองว่า API ตอบกลับมาสำเร็จ
        TuAuthResponse mockSuccessResponse = new TuAuthResponse();
        mockSuccessResponse.setStatus(true);
        // สมมติว่ามีฟิลด์ message ใน DTO ของคุณ
        mockSuccessResponse.setMessage("Success"); 

        ResponseEntity<TuAuthResponse> responseEntity = new ResponseEntity<>(mockSuccessResponse, HttpStatus.OK);

        // ให้ RestTemplate คืนค่า 200 OK
        when(restTemplate.postForEntity(eq(dummyApiUrl), any(HttpEntity.class), eq(TuAuthResponse.class)))
                .thenReturn(responseEntity);

        // 2. Act
        TuAuthResponse result = tuAuthService.authenticate("6500000000", "password123");

        // 3. Assert
        assertThat(result).isNotNull();
        assertThat(result.isStatus()).isTrue();
    }

    @Test
    @DisplayName("Should return parsed error response when API returns HTTP error with JSON") // ยิง API แล้วเกิด HTTP ERROR 401 คืนค่า JSON ที่อ่านได้
    void testAuthenticate_HttpClientError_WithParsableJson() {
        // 1. Arrange: จำลอง Exception ที่โดนโยนออกมา
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        
        TuAuthResponse mockErrorResponse = new TuAuthResponse();
        mockErrorResponse.setStatus(false);
        mockErrorResponse.setMessage("Invalid credentials");

        // ให้ Exception คืนค่าโครงสร้าง JSON ที่อ่านได้
        when(mockException.getResponseBodyAs(TuAuthResponse.class)).thenReturn(mockErrorResponse);

        // ให้ RestTemplate โยน Exception ออกมา
        when(restTemplate.postForEntity(eq(dummyApiUrl), any(HttpEntity.class), eq(TuAuthResponse.class)))
                .thenThrow(mockException);

        // 2. Act
        TuAuthResponse result = tuAuthService.authenticate("6500000000", "wrong_pass");

        // 3. Assert
        assertThat(result.isStatus()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("Should return generic error response when API returns HTTP error with unparsable JSON") // ยิง API แล้วเกิด HHTP ERROR แต่่ไฟล์ JSON พัง ขึ้นแค่ Generic Error
    void testAuthenticate_HttpClientError_UnparsableJson() {
        // 1. Arrange
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        
        // จำลองให้ตอนพยายามอ่าน JSON แล้วพัง
        when(mockException.getResponseBodyAs(TuAuthResponse.class)).thenThrow(new RuntimeException("Parse Error"));
        when(mockException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(mockException.getResponseBodyAsString()).thenReturn("Bad Request Format");

        when(restTemplate.postForEntity(eq(dummyApiUrl), any(HttpEntity.class), eq(TuAuthResponse.class)))
                .thenThrow(mockException);

        // 2. Act
        TuAuthResponse result = tuAuthService.authenticate("invalid", "invalid");

        // 3. Assert
        assertThat(result.isStatus()).isFalse();
        assertThat(result.getMessage()).contains("Error 400 BAD_REQUEST: Bad Request Format");
    }

    @Test
    @DisplayName("Should return connection error response when server is unreachable") // เชื่อมต่อ TU API ไม่ได้กรณีเน็ตหลุด หรือเซิฟล่ม
    void testAuthenticate_ConnectionError() {
        // 1. Arrange: โยน RestClientException
        when(restTemplate.postForEntity(eq(dummyApiUrl), any(HttpEntity.class), eq(TuAuthResponse.class)))
                .thenThrow(new RestClientException("Connection Timeout"));

        // 2. Act
        TuAuthResponse result = tuAuthService.authenticate("user", "pass");

        // 3. Assert
        assertThat(result.isStatus()).isFalse();
        assertThat(result.getMessage()).contains("ไม่สามารถติดต่อเซิร์ฟเวอร์ TU API ได้");
    }
}