package com.union.union.domain.publisher.service;

import com.union.union.domain.publisher.dto.PublisherApplyRequestDto;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.entity.PublisherRole;
import com.union.union.domain.publisher.entity.PublisherStatus;
import com.union.union.domain.publisher.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;

    @Transactional
    public void apply(PublisherApplyRequestDto request) {
        if (publisherRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다");
        }

        Publisher publisher = Publisher.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password()) // 실제로는 인코딩 권장
                .status(PublisherStatus.PENDING)
                .role(PublisherRole.ROLE_USER)
                .build();

        publisherRepository.save(publisher);
        log.info("퍼블리셔 등록 완료. email={}, name={}", request.email(), request.name());
    }

    @Transactional
    public void approve(UUID id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("퍼블리셔를 찾을 수 없습니다"));

        publisher.approve();
        log.info("퍼블리셔 승인 완료. id={}, name={}", id, publisher.getName());
    }
}
