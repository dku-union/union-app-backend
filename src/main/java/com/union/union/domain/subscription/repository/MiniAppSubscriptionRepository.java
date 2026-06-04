package com.union.union.domain.subscription.repository;

import com.union.union.domain.subscription.entity.MiniAppSubscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MiniAppSubscriptionRepository extends JpaRepository<MiniAppSubscription, Long> {

    Optional<MiniAppSubscription> findByUser_IdAndMiniApp_Id(UUID userId, Long miniAppId);

    @Query("SELECT s FROM MiniAppSubscription s " +
            "JOIN FETCH s.miniApp m " +
            "WHERE s.user.id = :userId AND s.unsubscribedAt IS NULL " +
            "ORDER BY s.subscribedAt DESC")
    List<MiniAppSubscription> findActiveByUser(@Param("userId") UUID userId);

    @Query("SELECT s FROM MiniAppSubscription s " +
            "WHERE s.miniApp.id = :miniAppId " +
            "AND s.pushEnabled = true " +
            "AND s.unsubscribedAt IS NULL")
    List<MiniAppSubscription> findActiveSubscribersOfMiniApp(
            @Param("miniAppId") Long miniAppId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM MiniAppSubscription s " +
            "WHERE s.miniApp.id = :miniAppId " +
            "AND s.pushEnabled = true " +
            "AND s.unsubscribedAt IS NULL")
    long countActiveSubscribersOfMiniApp(@Param("miniAppId") Long miniAppId);

    /**
     * 사용자의 활성 구독을 모두 해지 처리한다(soft). 회원 탈퇴 시 호출.
     * {@code unsubscribe()} 시맨틱과 동일하게 unsubscribedAt 을 채우고 pushEnabled 를 끈다.
     * @return 해지된 행 수
     */
    @Modifying
    @Query("UPDATE MiniAppSubscription s " +
            "SET s.unsubscribedAt = CURRENT_TIMESTAMP, s.pushEnabled = false " +
            "WHERE s.user.id = :userId AND s.unsubscribedAt IS NULL")
    int deactivateAllByUserId(@Param("userId") UUID userId);
}
