package com.union.union.domain.workspace.entity;

import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Id
    @Column(name = "workspace_id", nullable = false, columnDefinition = "uuid")
    private UUID workspaceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email", nullable = false, length = 100)
    private String contactEmail;

    @Column(nullable = false, length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher owner;

    @Builder
    public Workspace(UUID workspaceId, String name, String description, String contactEmail, String color, Publisher owner) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.contactEmail = contactEmail;
        this.color = color;
        this.owner = owner;
    }
}
