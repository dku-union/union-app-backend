package com.union.union.domain.workspace.entity;

import com.union.union.domain.publisher.entity.Publisher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "workspace_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workspace_members_workspace_publisher", columnNames = {"workspace_id", "publisher_id"})
        },
        indexes = {
                @Index(name = "idx_workspace_members_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_workspace_members_publisher_id", columnList = "publisher_id")
        }
)
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(nullable = false, length = 50)
    private String role;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public WorkspaceMember(Workspace workspace, Publisher publisher, String role) {
        this.workspace = workspace;
        this.publisher = publisher;
        this.role = role;
    }
}
