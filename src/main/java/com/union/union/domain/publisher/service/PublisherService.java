package com.union.union.domain.publisher.service;

import com.union.union.domain.publisher.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;

    // TODO: Publisher 인증은 Next.js (NextAuth) 기반으로 전환됨. 아래 로직은 User-Publisher 혼동 코드로 사용하지 않음.
    // @Transactional
    // public void apply(PublisherApplyRequestDto request, UUID userId) {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    //
    //     if (publisherRepository.existsByPublisherId(userId)) {
    //         throw new IllegalArgumentException("이미 퍼블리셔 신청을 하셨거나 프로필이 존재합니다");
    //     }
    //
    //     Publisher publisher = Publisher.builder()
    //             .publisherId(userId)
    //             .name(request.name())
    //             .email(request.contactEmail())
    //             .password(passwordEncoder.encode(user.getPassword()))
    //             .description(request.description())
    //             .pubstatus(PublisherStatus.PENDING)
    //             .role(user.getRole().name())
    //             .build();
    //
    //     publisherRepository.save(publisher);
    //     user.updateRole(User.Role.ROLE_PUBLISHER);
    //     log.info("퍼블리셔 신청 완료. userId={}, publisherName={}", userId, publisher.getName());
    // }

    // @Transactional
    // public void approve(Long id) {
    //     Publisher publisher = publisherRepository.findById(id)
    //             .orElseThrow(() -> new IllegalArgumentException("퍼블리셔를 찾을 수 없습니다"));
    //
    //     publisher.approve();
    //     log.info("퍼블리셔 승인 완료. id={}, name={}", id, publisher.getName());
    // }
}
