package com.union.union.global.infra.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FcmService {

    public void sendMulticast(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase 미초기화 — 알림 스킵. title={}", title);
            return;
        }
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(tokens)
                    .build();
            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM 발송 완료. success={}, failure={}, title={}",
                    response.getSuccessCount(), response.getFailureCount(), title);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: {}", e.getMessage());
        }
    }
}
