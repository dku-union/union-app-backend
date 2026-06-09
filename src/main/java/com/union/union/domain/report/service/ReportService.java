package com.union.union.domain.report.service;

import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.report.dto.CreateReportRequestDto;
import com.union.union.domain.report.dto.ReportResponseDto;
import com.union.union.domain.report.entity.Report;
import com.union.union.domain.report.entity.ReportTargetType;
import com.union.union.domain.report.repository.ReportRepository;
import com.union.union.domain.review.repository.ReviewRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.BadRequestException;
import com.union.union.global.common.exception.ConflictException;
import com.union.union.global.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MiniAppRepository miniAppRepository;
    private final ReviewRepository reviewRepository;

    public ReportResponseDto createReport(UUID reporterUserId, CreateReportRequestDto request) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        validateTarget(request);
        checkDuplicate(reporterUserId, request);

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetMiniAppId(request.targetMiniAppId())
                .targetReviewId(request.targetReviewId())
                .reportedUserId(request.reportedUserId())
                .reason(request.reason())
                .detail(request.detail())
                .build();

        reportRepository.save(report);
        log.info("신고 접수 완료. reportId={}, targetType={}, reporterId={}", report.getId(), request.targetType(), reporterUserId);

        return ReportResponseDto.from(report);
    }

    private void validateTarget(CreateReportRequestDto request) {
        switch (request.targetType()) {
            case MINI_APP -> {
                if (request.targetMiniAppId() == null)
                    throw new BadRequestException("MINI_APP 신고는 targetMiniAppId가 필수입니다");
                if (!miniAppRepository.existsById(request.targetMiniAppId()))
                    throw new EntityNotFoundException("존재하지 않는 미니앱입니다");
            }
            case REVIEW -> {
                if (request.targetReviewId() == null)
                    throw new BadRequestException("REVIEW 신고는 targetReviewId가 필수입니다");
                if (!reviewRepository.existsById(request.targetReviewId()))
                    throw new EntityNotFoundException("존재하지 않는 리뷰입니다");
            }
            case USER -> {
                if (request.reportedUserId() == null)
                    throw new BadRequestException("USER 신고는 reportedUserId가 필수입니다");
                if (!userRepository.existsById(request.reportedUserId()))
                    throw new EntityNotFoundException("존재하지 않는 사용자입니다");
            }
        }
    }

    private void checkDuplicate(UUID reporterUserId, CreateReportRequestDto request) {
        boolean exists = reportRepository.existsActiveDuplicate(
                reporterUserId,
                request.targetType(),
                request.targetMiniAppId(),
                request.targetReviewId(),
                request.reportedUserId()
        );
        if (exists) throw new ConflictException("이미 처리 중인 신고가 있습니다");
    }
}
