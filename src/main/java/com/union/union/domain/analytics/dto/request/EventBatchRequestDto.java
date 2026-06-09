package com.union.union.domain.analytics.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Analytics 이벤트 배치 수신 요청 DTO.
 *
 * <p>네이티브 앱은 최대 100개 이벤트를 하나의 배치로 묶어 전송한다.
 * 배치 내 모든 이벤트는 동일한 앱({@code X-Union-AppId} 헤더)에 속해야 한다.
 */
public record EventBatchRequestDto(

    @NotEmpty(message = "events must not be empty")
    @Size(max = 100, message = "Maximum 100 events per batch")
    List<@Valid RawEventDto> events

) {}
