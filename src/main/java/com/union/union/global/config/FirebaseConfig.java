package com.union.union.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credential-path:}")
    private String credentialPath;

    @PostConstruct
    public void init() {
        if (credentialPath.isBlank()) {
            log.warn("firebase.credential-path 미설정 — 푸쉬 알림 비활성화 상태로 기동");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try (FileInputStream serviceAccount = new FileInputStream(credentialPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료. credential={}", credentialPath);
        } catch (IOException e) {
            log.error("Firebase 초기화 실패 — 푸쉬 알림 비활성화: {}", e.getMessage());
        }
    }
}
