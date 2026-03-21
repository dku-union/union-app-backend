package com.union.union.domain.university.controller;

import com.union.union.domain.university.dto.UniversityResponseDto;
import com.union.union.domain.university.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/universities")
    public ResponseEntity<List<UniversityResponseDto>> searchUniversities(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        List<UniversityResponseDto> results = universityService.searchUniversities(keyword);
        return ResponseEntity.ok(results);
    }
}
