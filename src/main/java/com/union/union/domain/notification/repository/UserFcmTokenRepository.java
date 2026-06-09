package com.union.union.domain.notification.repository;

import com.union.union.domain.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    @Query("SELECT t.token FROM UserFcmToken t WHERE t.user.id = :userId")
    List<String> findTokensByUserId(@Param("userId") UUID userId);

    Optional<UserFcmToken> findByToken(String token);

    Optional<UserFcmToken> findByUser_IdAndDeviceId(UUID userId, String deviceId);

    @Modifying
    @Query("DELETE FROM UserFcmToken t WHERE t.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM UserFcmToken t WHERE t.user.id = :userId AND t.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") String deviceId);

    /**
     * 사용자의 모든 FCM 토큰을 일괄 삭제한다. 회원 탈퇴 시 호출 — 탈퇴한 기기로 푸시가 계속 가지 않도록.
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM UserFcmToken t WHERE t.user.id = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
}
