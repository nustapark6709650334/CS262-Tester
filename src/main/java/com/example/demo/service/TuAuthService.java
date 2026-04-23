package com.example.demo.service;

import com.example.demo.dto.TuAuthRequest;
import com.example.demo.dto.TuAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class TuAuthService {

    @Value("${tu.api.url}")
    private String tuApiUrl;

    @Value("${tu.api.key}")
    private String tuApiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public TuAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TuAuthResponse authenticate(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Application-Key", tuApiKey);

        TuAuthRequest requestBody = new TuAuthRequest(username, password);
        HttpEntity<TuAuthRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TuAuthResponse> response = restTemplate.postForEntity(
                tuApiUrl, entity, TuAuthResponse.class
            );
            return response.getBody();

        } catch (HttpClientErrorException e) {
            // พยายามอ่าน JSON Error ที่ TU API อาจส่งกลับมา
            try {
                TuAuthResponse errorResponse = e.getResponseBodyAs(TuAuthResponse.class);
                if (errorResponse != null) {
                    return errorResponse;
                }
            } catch (Exception ex) {
                // ไม่สามารถแปลง Error JSON ได้
            }
            TuAuthResponse genericError = new TuAuthResponse();
            genericError.setStatus(false);
            genericError.setMessage("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            return genericError;
            
        } catch (RestClientException e) {
            // เชื่อมต่อ API ไม่ได้
            TuAuthResponse connError = new TuAuthResponse();
            connError.setStatus(false);
            connError.setMessage("ไม่สามารถติดต่อเซิร์ฟเวอร์ TU API ได้: " + e.getMessage());
            return connError;
        }
    }
}