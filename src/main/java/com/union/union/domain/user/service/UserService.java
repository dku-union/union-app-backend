package com.union.union.domain.user.service;

import com.union.union.domain.user.dto.ProfileImageUploadUrlRequestDto;
import com.union.union.domain.user.dto.UpdateNicknameRequestDto;
import com.union.union.domain.user.dto.UpdateProfileImageRequestDto;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.infra.gcs.GcsService;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GcsService gcsService;

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateNickname(UUID userId, UpdateNicknameRequestDto request) {
        User user = getUser(userId);
        user.updateProfile(request.nickname(), null);
        return user;
    }

    public GcsSignedUrlResponseDto getProfileImageUploadUrl(UUID userId, ProfileImageUploadUrlRequestDto request) {
        getUser(userId); // 존재 여부 검증
        return gcsService.getProfileImageUploadUrl(userId, request.filename());
    }

    @Transactional
    public User updateProfileImage(UUID userId, UpdateProfileImageRequestDto request) {
        User user = getUser(userId);
        user.updateProfile(null, request.imageUrl());
        return user;
    }

    @Transactional
    public void withdrawMe(UUID userId) {
        User user = getUser(userId);
        user.withdraw();
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getUser(id);
        user.withdraw();
    }
}
