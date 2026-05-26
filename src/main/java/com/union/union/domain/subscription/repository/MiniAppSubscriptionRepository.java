package com.union.union.domain.subscription.repository;

import com.union.union.domain.subscription.entity.MiniAppSubscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
