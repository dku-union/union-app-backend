package com.union.union.domain.miniapp.entity;

import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.university.entity.UniversityDomain;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mini_app")
public class MiniApp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String iconUrl;

    @Column(nullable = false, length = 500)
    private String launchUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private UniversityDomain university;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MiniAppStatus status;

    @Builder
    public MiniApp(String name, String description, String iconUrl, String launchUrl, 
                   Publisher publisher, UniversityDomain university, MiniAppStatus status) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.launchUrl = launchUrl;
        this.publisher = publisher;
        this.university = university;
        this.status = status != null ? status : MiniAppStatus.PENDING;
    }

    public void approve() {
        this.status = MiniAppStatus.APPROVED;
    }
}
