package com.union.union.domain.report.controller;

import com.union.union.domain.report.dto.CreateReportRequestDto;
import com.union.union.domain.report.dto.ReportResponseDto;
import com.union.union.domain.report.service.ReportService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @Valid @RequestBody CreateReportRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        ReportResponseDto response = reportService.createReport(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
