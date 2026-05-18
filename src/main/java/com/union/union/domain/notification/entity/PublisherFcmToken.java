package com.union.union.domain.notification.entity;

import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "publisher_fcm_tokens")
public class PublisherFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Builder
    public PublisherFcmToken(Publisher publisher, String token) {
        this.publisher = publisher;
        this.token = token;
    }
}
