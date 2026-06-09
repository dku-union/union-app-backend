package com.union.union.domain.notification.repository;

import com.union.union.domain.notification.entity.NotificationInbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationInboxRepository extends JpaRepository<NotificationInbox, Long> {

    @Query("SELECT n FROM NotificationInbox n JOIN FETCH n.campaign WHERE n.user.id = :userId AND (:cursor IS NULL OR n.id < :cursor) ORDER BY n.id DESC")
    List<NotificationInbox> findByUserIdWithCursor(@Param("userId") UUID userId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificationInbox n WHERE n.user.id = :userId AND n.read = false")
    long countUnreadByUserId(@Param("userId") UUID userId);

    @Query("SELECT n FROM NotificationInbox n WHERE n.user.id = :userId AND n.read = false")
    List<NotificationInbox> findUnreadByUserId(@Param("userId") UUID userId);
}
