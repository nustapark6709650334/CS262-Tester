package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() throws Exception {
        // สร้างกฏปลอมขึ้นมาบอกให้ระบบ "เชื่อถือ Certificate ทุกเว็บ"
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        // ยัดกฏปลอมเข้าไปในระบบ SSL ของ Java
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // สั่งให้ข้ามการตรวจสอบชื่อ Hostname ด้วย
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // ส่ง RestTemplate ออกไปให้ TuAuthService ใช้งาน
        return new RestTemplate();
    }
}