package com.union.union.domain.appversion.entity;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.user.entity.User;
import com.union.union.global.common.exception.ConflictException;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "app_versions")
public class AppVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_app_id", nullable = false)
    private MiniApp miniApp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;

    @Column(nullable = false, length = 20)
    private String versionNumber;

    @Column(columnDefinition = "TEXT")
    private String releaseNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VersionStatus status;

    @Column(length = 500)
    private String buildFileUrl;

    private Long bundleSize;

    private LocalDateTime testedAt;

    @Builder
    public AppVersion(MiniApp miniApp, User publisher, String versionNumber, String releaseNotes) {
        this.miniApp = miniApp;
        this.publisher = publisher;
        this.versionNumber = versionNumber;
        this.releaseNotes = releaseNotes;
        this.status = VersionStatus.DRAFT;
    }

    public void confirmUpload(String buildFileUrl, Long bundleSize) {
        if (this.status != VersionStatus.DRAFT) {
            throw new ConflictException("DRAFT 상태에서만 업로드를 확정할 수 있습니다");
        }
        this.buildFileUrl = buildFileUrl;
        this.bundleSize = bundleSize;
        this.status = VersionStatus.UPLOADED;
    }

    public void markTested() {
        if (this.testedAt == null) {
            this.testedAt = LocalDateTime.now();
        }
    }

    public void submitForReview() {
        if (this.status != VersionStatus.UPLOADED) {
            throw new ConflictException("UPLOADED 상태에서만 심사를 요청할 수 있습니다");
        }
        if (this.testedAt == null) {
            throw new ConflictException("최소 1회 테스트 후 심사를 요청할 수 있습니다");
        }
        this.status = VersionStatus.IN_REVIEW;
    }

    public void accept() {
        if (this.status != VersionStatus.IN_REVIEW) {
            throw new ConflictException("IN_REVIEW 상태에서만 승인할 수 있습니다");
        }
        this.status = VersionStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != VersionStatus.IN_REVIEW) {
            throw new ConflictException("IN_REVIEW 상태에서만 거절할 수 있습니다");
        }
        this.status = VersionStatus.REJECTED;
    }

    public void deploy() {
        if (this.status != VersionStatus.ACCEPTED) {
            throw new ConflictException("ACCEPTED 상태에서만 배포할 수 있습니다");
        }
        this.status = VersionStatus.DEPLOYED;
    }
}
