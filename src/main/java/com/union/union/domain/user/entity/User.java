package com.union.union.domain.user.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 100)
    private String universityName;

    @Column(nullable = false)
    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    public User(String email, String password, String nickname, String universityName) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.universityName = universityName;
        this.isVerified = false;
        this.userStatus = UserStatus.ACTIVE;
        this.role = Role.ROLE_USER;
    }

    public void verify() {
        this.isVerified = true;
    }

    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null) this.nickname = nickname;
        if (profileImage != null) this.profileImage = profileImage;
    }

    public void suspend() {
        this.userStatus = UserStatus.SUSPENDED;
    }

    public void withdraw() {
        this.userStatus = UserStatus.WITHDRAWN;
    }

    /**
     * 탈퇴 시 unique 제약이 걸린 email 을 해제(tombstone 치환)해 동일 이메일로 재가입할 수 있게 한다.
     * 탈퇴는 영구 조치이므로 원본 email 은 보존하지 않으며, 프로필 이미지 참조도 함께 비운다.
     * id 기반 tombstone 이라 항상 유일하다.
     */
    public void anonymizeOnWithdrawal() {
        this.email = "withdrawn+" + this.id + "@deleted.union.app";
        this.profileImage = null;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public enum Role {
        ROLE_USER, ROLE_TESTER, ROLE_ADMIN
    }

    public enum UserStatus {
        ACTIVE, SUSPENDED, WITHDRAWN
    }
}
