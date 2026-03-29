package com.union.union.domain.publisher.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "publisher")
public class Publisher extends BaseEntity {

    @Id
    @Column(name = "publisher_id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "pubstatus", nullable = false, length = 20)
    private PublisherStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private PublisherRole role;

    @Builder
    public Publisher(UUID id, String name, String email, String password, PublisherStatus status, PublisherRole role) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status != null ? status : PublisherStatus.PENDING;
        this.role = role != null ? role : PublisherRole.ROLE_USER;
    }

    public void approve() {
        this.status = PublisherStatus.APPROVED;
    }

    public void activate() {
        this.status = PublisherStatus.ACTIVE;
    }

    public void reject() {
        this.status = PublisherStatus.REJECTED;
    }
}
