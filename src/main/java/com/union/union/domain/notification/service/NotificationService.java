package com.union.union.domain.notification.service;

import com.union.union.domain.notification.entity.PublisherFcmToken;
import com.union.union.domain.notification.entity.UserFcmToken;
import com.union.union.domain.notification.repository.PublisherFcmTokenRepository;
import com.union.union.domain.notification.repository.UserFcmTokenRepository;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.infra.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final PublisherFcmTokenRepository publisherFcmTokenRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final PublisherRepository publisherRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    public void registerPublisherToken(UUID publisherId, String token) {
        if (publisherFcmTokenRepository.findByToken(token).isPresent()) {
            return;
        }
        Publisher publisher = publisherRepository.findByPublisherId(publisherId)
                .orElseThrow(() -> new EntityNotFoundException("Publisher를 찾을 수 없습니다"));
        publisherFcmTokenRepository.save(PublisherFcmToken.builder()
                .publisher(publisher)
                .token(token)
                .build());
        log.info("Publisher FCM 토큰 등록. publisherId={}", publisherId);
    }

    public void registerUserToken(UUID userId, String token) {
        if (userFcmTokenRepository.findByToken(token).isPresent()) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User를 찾을 수 없습니다"));
        userFcmTokenRepository.save(UserFcmToken.builder()
                .user(user)
                .token(token)
                .build());
        log.info("User FCM 토큰 등록. userId={}", userId);
    }

    public void deleteToken(String token) {
        publisherFcmTokenRepository.deleteByToken(token);
        userFcmTokenRepository.deleteByToken(token);
    }

    @Transactional(readOnly = true)
    public void sendToPublisher(UUID publisherId, String title, String body) {
        List<String> tokens = publisherFcmTokenRepository.findTokensByPublisherId(publisherId);
        fcmService.sendMulticast(tokens, title, body);
    }

    @Transactional(readOnly = true)
    public void sendToUser(UUID userId, String title, String body) {
        List<String> tokens = userFcmTokenRepository.findTokensByUserId(userId);
        fcmService.sendMulticast(tokens, title, body);
    }
}
