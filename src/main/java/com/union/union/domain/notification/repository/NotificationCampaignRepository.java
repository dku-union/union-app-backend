package com.union.union.domain.notification.repository;

import com.union.union.domain.notification.entity.NotificationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationCampaignRepository extends JpaRepository<NotificationCampaign, Long> {
}
