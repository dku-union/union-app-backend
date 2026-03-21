package com.union.union.domain.university.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 커리어넷 외부 API 응답을 파싱하기 위한 DTO (JSON 구조 그대로 매핑)
 */
public record ExternalUniversityResponseDto(
    @JsonProperty("dataSearch") DataSearch dataSearch
) {

    public record DataSearch(
        @JsonProperty("content") List<Content> content
    ) {}

    public record Content(
        @JsonProperty("schoolName") String schoolName,
        @JsonProperty("adres") String address,
        @JsonProperty("link") String link,
        @JsonProperty("schoolGubun") String schoolType
    ) {}
}
