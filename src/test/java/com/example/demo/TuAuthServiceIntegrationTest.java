package com.example.demo;

import com.example.demo.config.AppConfig;
import com.example.demo.dto.TuAuthResponse;
import com.example.demo.service.TuAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(classes = { TuAuthService.class, AppConfig.class }, properties = { "tu.api.url=http://mock-tu-api/auth",
		"tu.api.key=test-api-key" })
class TuAuthServiceIntegrationTest {

	@Autowired
	private TuAuthService tuAuthService;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	@BeforeEach
	void setUp() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	void INT_EXT_01_externalAuthenticationApiSuccessPath() {
		String responseJson = """
				{
				  "status": true,
				  "message": "success",
				  "displayname_th": "ทดสอบ ระบบ",
				  "email": "test@tu.ac.th",
				  "type": "student",
				  "username": "testuser"
				}
				""";

		mockServer.expect(once(), requestTo("http://mock-tu-api/auth"))
				.andExpect(header("Application-Key", "test-api-key"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

		TuAuthResponse response = tuAuthService.authenticate("testuser", "password123");

		assertNotNull(response);
		assertTrue(response.isStatus());
		assertEquals("ทดสอบ ระบบ", response.getDisplayNameTh());
		assertEquals("test@tu.ac.th", response.getEmail());
		assertEquals("testuser", response.getUsername());

		mockServer.verify();
	}

	@Test
	void INT_EXT_02_externalAuthenticationApiFailureResponse() {
		String errorJson = """
				{
				  "status": false,
				  "message": "Username หรือ Password ไม่ถูกต้อง"
				}
				""";

		mockServer.expect(once(), requestTo("http://mock-tu-api/auth"))
				.andRespond(withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(errorJson));

		TuAuthResponse response = tuAuthService.authenticate("wronguser", "wrongpass");

		assertNotNull(response);
		assertFalse(response.isStatus());
		assertEquals("Username หรือ Password ไม่ถูกต้อง", response.getMessage());

		mockServer.verify();
	}

	@Test
	void INT_EXT_03_externalApiTimeout() {
		mockServer.expect(once(), requestTo("http://mock-tu-api/auth")).andRespond(request -> {
			throw new ResourceAccessException("Read timed out");
		});

		TuAuthResponse response = tuAuthService.authenticate("testuser", "password123");

		assertNotNull(response);
		assertFalse(response.isStatus());
		assertTrue(response.getMessage().contains("ไม่สามารถติดต่อเซิร์ฟเวอร์ TU API ได้"));

		mockServer.verify();
	}

	@Test
	void INT_EXT_04_malformedExternalApiResponse() {
		String malformedJson = """
				{
				  "status": true,
				  "displayname_th":
				}
				""";

		mockServer.expect(once(), requestTo("http://mock-tu-api/auth"))
				.andRespond(withSuccess(malformedJson, MediaType.APPLICATION_JSON));

		TuAuthResponse response = tuAuthService.authenticate("testuser", "password123");

		assertNotNull(response);
		assertFalse(response.isStatus());
		assertTrue(response.getMessage().contains("ไม่สามารถติดต่อเซิร์ฟเวอร์ TU API ได้"));

		mockServer.verify();
	}
}
