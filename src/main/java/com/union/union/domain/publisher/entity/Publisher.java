package com.union.union.domain.publisher.entity;

import com.union.union.domain.user.entity.User;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "publishers")
public class Publisher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublisherStatus status;

    @Builder
    public Publisher(User user, String name, String description, String contactEmail, PublisherStatus status) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.contactEmail = contactEmail;
        this.status = status != null ? status : PublisherStatus.PENDING;
    }

    public void approve() {
        this.status = PublisherStatus.APPROVED;
    }

    public void reject() {
        this.status = PublisherStatus.REJECTED;
    }
}
