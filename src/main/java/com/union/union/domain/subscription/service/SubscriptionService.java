package com.union.union.domain.subscription.service;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.subscription.entity.MiniAppSubscription;
import com.union.union.domain.subscription.repository.MiniAppSubscriptionRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final MiniAppSubscriptionRepository subscriptionRepository;
    private final MiniAppRepository miniAppRepository;
    private final UserRepository userRepository;

    /**
     * 미니앱 실행 시 자동 구독. 이미 활성 구독이 있으면 no-op,
     * 폐기된 구독이 있으면 재활성화, 없으면 신규 생성.
     */
    public void autoSubscribe(UUID userId, Long miniAppId) {
        subscriptionRepository.findByUser_IdAndMiniApp_Id(userId, miniAppId)
                .ifPresentOrElse(
                        existing -> {
                            if (!existing.isActive()) {
                                existing.reactivate();
                                log.info("미니앱 구독 재활성화. userId={}, miniAppId={}", userId, miniAppId);
                            }
                        },
                        () -> {
                            User user = userRepository.findById(userId)
                                    .orElseThrow(() -> new EntityNotFoundException("User를 찾을 수 없습니다"));
                            MiniApp miniApp = miniAppRepository.findById(miniAppId)
                                    .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다"));
                            subscriptionRepository.save(MiniAppSubscription.builder()
                                    .user(user)
                                    .miniApp(miniApp)
                                    .build());
                            log.info("미니앱 신규 구독. userId={}, miniAppId={}", userId, miniAppId);
                        }
                );
    }

    public void subscribeByAppId(UUID userId, String appId) {
        MiniApp miniApp = miniAppRepository.findByAppId(appId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. appId=" + appId));
        autoSubscribe(userId, miniApp.getId());
    }

    public void setPushEnabled(UUID userId, String appId, boolean enabled) {
        MiniApp miniApp = miniAppRepository.findByAppId(appId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. appId=" + appId));
        MiniAppSubscription subscription = subscriptionRepository
                .findByUser_IdAndMiniApp_Id(userId, miniApp.getId())
                .orElseThrow(() -> new EntityNotFoundException("구독을 찾을 수 없습니다"));
        if (enabled && !subscription.isActive()) {
            subscription.reactivate();
        }
        subscription.setPushEnabled(enabled);
        log.info("푸시 토글. userId={}, appId={}, enabled={}", userId, appId, enabled);
    }

    public void unsubscribe(UUID userId, String appId) {
        MiniApp miniApp = miniAppRepository.findByAppId(appId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. appId=" + appId));
        subscriptionRepository.findByUser_IdAndMiniApp_Id(userId, miniApp.getId())
                .ifPresent(s -> {
                    s.unsubscribe();
                    log.info("미니앱 구독 해지. userId={}, appId={}", userId, appId);
                });
    }

    @Transactional(readOnly = true)
    public List<MiniAppSubscription> listActiveByUser(UUID userId) {
        return subscriptionRepository.findActiveByUser(userId);
    }

    @Transactional(readOnly = true)
    public List<MiniAppSubscription> listActiveSubscribers(Long miniAppId, Pageable pageable) {
        return subscriptionRepository.findActiveSubscribersOfMiniApp(miniAppId, pageable);
    }

    @Transactional(readOnly = true)
    public long countActiveSubscribers(Long miniAppId) {
        return subscriptionRepository.countActiveSubscribersOfMiniApp(miniAppId);
    }
}
