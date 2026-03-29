package com.union.union.domain.publisher.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "publishers")
@EntityListeners(AuditingEntityListener.class)
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publisher_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID publisherId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pubstatus", nullable = false, length = 20)
    private PublisherStatus pubstatus;

    @Column(nullable = false, length = 20)
    private String role;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Publisher(UUID publisherId, String name, String email, String password,
                     String description, PublisherStatus pubstatus, String role) {
        this.publisherId = publisherId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.description = description;
        this.pubstatus = pubstatus != null ? pubstatus : PublisherStatus.PENDING;
        this.role = role != null ? role : "ROLE_USER";
    }

    public void approve() {
        this.pubstatus = PublisherStatus.ACTIVE;
    }

    public void reject() {
        this.pubstatus = PublisherStatus.REJECTED;
    }
}
