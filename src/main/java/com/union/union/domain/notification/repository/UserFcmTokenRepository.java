package com.union.union.domain.notification.repository;

import com.union.union.domain.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
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

    void deleteByToken(String token);
}
