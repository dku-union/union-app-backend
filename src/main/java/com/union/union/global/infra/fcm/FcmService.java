package com.union.union.global.infra.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FcmService {

    public void sendMulticast(List<String> tokens, String title, String body) {
        sendMulticastWithData(tokens, title, body, Map.of());
    }

    public void sendMulticastWithData(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) return;
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase 미초기화 — 알림 스킵. title={}", title);
            return;
        }
        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .addAllTokens(tokens);

            if (data.containsKey("category")) {
                builder.setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setMutableContent(true)
                                .setCategory(data.get("category"))
                                .build())
                        .build());
                builder.setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setChannelId(data.getOrDefault("category", "default").toLowerCase())
                                .build())
                        .build());
            }

            var response = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            log.info("FCM 발송 완료. success={}, failure={}, title={}",
                    response.getSuccessCount(), response.getFailureCount(), title);

            handleFailedTokens(tokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: {}", e.getMessage());
        }
    }

    private void handleFailedTokens(List<String> tokens, BatchResponse response) {
        var responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            if (!r.isSuccessful() && r.getException() != null) {
                MessagingErrorCode errorCode = r.getException().getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    log.warn("FCM 유효하지 않은 토큰 감지 (삭제 필요): token={}", tokens.get(i));
                }
            }
        }
    }
}
