package com.union.union.domain.report.controller;

import com.union.union.domain.report.dto.AdminReportResponseDto;
import com.union.union.domain.report.dto.ProcessReportRequestDto;
import com.union.union.domain.report.entity.ReportStatus;
import com.union.union.domain.report.entity.ReportTargetType;
import com.union.union.domain.report.service.AdminReportService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<Page<AdminReportResponseDto>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminReportService.getReports(status, targetType, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminReportResponseDto> processReport(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessReportRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(adminReportService.processReport(id, principal.userId(), request));
    }
}
