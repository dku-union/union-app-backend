package com.union.union.domain.notification.service;

import com.union.union.domain.notification.dto.InboxResponseDto;
import com.union.union.domain.notification.dto.RegisterTokenRequestDto;
import com.union.union.domain.notification.dto.SendNotificationRequestDto;
import com.union.union.domain.notification.entity.*;
import com.union.union.domain.notification.repository.*;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.infra.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final PublisherFcmTokenRepository publisherFcmTokenRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NotificationCampaignRepository campaignRepository;
    private final NotificationInboxRepository inboxRepository;
    private final PublisherRepository publisherRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    // ── 토큰 등록/삭제 ──────────────────────────────────────────

    public void upsertUserToken(UUID userId, RegisterTokenRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User를 찾을 수 없습니다"));

        userFcmTokenRepository.findByUser_IdAndDeviceId(userId, dto.deviceId())
                .ifPresentOrElse(
                        existing -> existing.upsert(dto.token(), dto.appVersion(), dto.osVersion()),
                        () -> userFcmTokenRepository.save(UserFcmToken.builder()
                                .user(user)
                                .deviceId(dto.deviceId())
                                .platform(dto.platform())
                                .token(dto.token())
                                .appVersion(dto.appVersion())
                                .osVersion(dto.osVersion())
                                .build())
                );
        log.info("User FCM 토큰 upsert. userId={}, deviceId={}", userId, dto.deviceId());
    }

    public void upsertPublisherToken(UUID publisherId, RegisterTokenRequestDto dto) {
        Publisher publisher = publisherRepository.findByPublisherId(publisherId)
                .orElseThrow(() -> new EntityNotFoundException("Publisher를 찾을 수 없습니다"));

        publisherFcmTokenRepository.findByPublisher_PublisherIdAndDeviceId(publisherId, dto.deviceId())
                .ifPresentOrElse(
                        existing -> existing.upsert(dto.token(), dto.appVersion(), dto.osVersion()),
                        () -> publisherFcmTokenRepository.save(PublisherFcmToken.builder()
                                .publisher(publisher)
                                .deviceId(dto.deviceId())
                                .platform(dto.platform())
                                .token(dto.token())
                                .appVersion(dto.appVersion())
                                .osVersion(dto.osVersion())
                                .build())
                );
        log.info("Publisher FCM 토큰 upsert. publisherId={}, deviceId={}", publisherId, dto.deviceId());
    }

    public void deleteToken(UUID requesterId, String role, String token, String deviceId) {
        if ("ROLE_PUBLISHER".equals(role) || "ROLE_ADMIN".equals(role)) {
            if (token != null) publisherFcmTokenRepository.deleteByToken(token);
            if (deviceId != null) publisherFcmTokenRepository.deleteByPublisherIdAndDeviceId(requesterId, deviceId);
        } else {
            if (token != null) userFcmTokenRepository.deleteByToken(token);
            if (deviceId != null) userFcmTokenRepository.deleteByUserIdAndDeviceId(requesterId, deviceId);
        }
    }

    // ── 인박스 조회/읽음 ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InboxResponseDto> getInbox(UUID userId, Long cursor, int limit) {
        return inboxRepository.findByUserIdWithCursor(userId, cursor, PageRequest.of(0, limit))
                .stream()
                .map(InboxResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return inboxRepository.countUnreadByUserId(userId);
    }

    public void markRead(Long inboxId, UUID userId) {
        NotificationInbox inbox = inboxRepository.findById(inboxId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다"));
        if (!inbox.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("알림을 찾을 수 없습니다");
        }
        inbox.markRead();
    }

    public void markAllRead(UUID userId) {
        inboxRepository.findUnreadByUserId(userId).forEach(NotificationInbox::markRead);
    }

    // ── 발송 ─────────────────────────────────────────────────────

    public void sendSystemNotification(SendNotificationRequestDto request, UUID adminId) {
        NotificationCampaign campaign = campaignRepository.save(NotificationCampaign.builder()
                .senderType(SenderType.SYSTEM)
                .senderPublisherId(adminId)
                .category(request.category())
                .title(request.title())
                .body(request.body())
                .imageUrl(request.imageUrl())
                .deeplinkType(request.deeplinkType())
                .targetAppId(request.targetAppId())
                .targetPath(request.targetPath())
                .targetWebUrl(request.targetWebUrl())
                .targetInternalRoute(request.targetInternalRoute())
                .build());

        fanOutToAllUsers(campaign);
    }

    @Async
    protected void fanOutToAllUsers(NotificationCampaign campaign) {
        int page = 0;
        int chunkSize = 1000;
        Map<String, String> data = buildFcmData(campaign);

        while (true) {
            List<User> users = userRepository.findAll(PageRequest.of(page, chunkSize)).getContent();
            if (users.isEmpty()) break;

            List<NotificationInbox> inboxes = users.stream()
                    .map(u -> NotificationInbox.builder().user(u).campaign(campaign).build())
                    .collect(Collectors.toList());
            inboxRepository.saveAll(inboxes);

            List<String> tokens = users.stream()
                    .flatMap(u -> userFcmTokenRepository.findTokensByUserId(u.getId()).stream())
                    .collect(Collectors.toList());

            if (!tokens.isEmpty()) {
                fcmService.sendMulticastWithData(tokens, campaign.getTitle(), campaign.getBody(), data);
            }

            if (users.size() < chunkSize) break;
            page++;
        }
        log.info("ALL_USERS fan-out 완료. campaignId={}", campaign.getId());
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

    private Map<String, String> buildFcmData(NotificationCampaign campaign) {
        Map<String, String> data = new HashMap<>();
        data.put("campaignId", String.valueOf(campaign.getId()));
        data.put("category", campaign.getCategory().name());
        data.put("deeplinkType", campaign.getDeeplinkType().name());
        if (campaign.getTargetAppId() != null) data.put("appId", campaign.getTargetAppId());
        if (campaign.getTargetPath() != null) data.put("path", campaign.getTargetPath());
        if (campaign.getTargetWebUrl() != null) data.put("webUrl", campaign.getTargetWebUrl());
        if (campaign.getTargetInternalRoute() != null) data.put("internalRoute", campaign.getTargetInternalRoute());
        return data;
    }
}
