package com.union.union.domain.report.service;

import com.union.union.domain.report.dto.AdminReportResponseDto;
import com.union.union.domain.report.dto.ProcessReportRequestDto;
import com.union.union.domain.report.entity.Report;
import com.union.union.domain.report.entity.ReportStatus;
import com.union.union.domain.report.entity.ReportTargetType;
import com.union.union.domain.report.repository.ReportRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;

    public Page<AdminReportResponseDto> getReports(ReportStatus status, ReportTargetType targetType, Pageable pageable) {
        return reportRepository.findWithFilters(status, targetType, pageable)
                .map(AdminReportResponseDto::from);
    }

    @Transactional
    public AdminReportResponseDto processReport(UUID reportId, UUID adminId, ProcessReportRequestDto request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("신고를 찾을 수 없습니다"));

        report.process(adminId, request.status(), request.actionTaken());

        log.info("신고 처리 완료. reportId={}, status={}, adminId={}", reportId, request.status(), adminId);
        return AdminReportResponseDto.from(report);
    }
}
