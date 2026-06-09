package com.union.union.domain.university.service;

import com.union.union.domain.university.dto.ExternalUniversityResponseDto;
import com.union.union.domain.university.dto.UniversityResponseDto;
import com.union.union.global.common.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Slf4j
@Service
public class UniversityService {

    private final RestTemplate restTemplate;

    // 외부에 노출되면 안 되는 인증키는 application.yml 파일로부터 주입받습니다.
    @Value("${careernet.api.key}")
    private String careernetApiKey;

    private static final String CAREERNET_BASE_URL = "https://www.career.go.kr/cnet/openapi/getOpenApi";

    public UniversityService() {
        // 실무에서는 외부 장애가 우리 서비스로 전파(Cascading Failure)되지 않게 타임아웃을 반드시 설정합니다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    public List<UniversityResponseDto> searchUniversities(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList(); // 키워드가 없으면 바로 빈 배열 반환
        }

        URI uri = UriComponentsBuilder.fromUriString(CAREERNET_BASE_URL)
                .queryParam("apiKey", careernetApiKey)
                .queryParam("svcType", "api")
                .queryParam("svcCode", "SCHOOL")
                .queryParam("contentType", "json")
                .queryParam("gubun", "univ_list")
                .queryParam("searchSchulNm", keyword)
                .build()
                .encode() // 한글 키워드(예: '연세')가 API 서버로 전송될 때 깨지지 않도록 반드시 인코딩해야 값이 검색됩니다!
                .toUri();

        try {
            ExternalUniversityResponseDto response = restTemplate.getForObject(uri,
                    ExternalUniversityResponseDto.class);

            return Optional.ofNullable(response)
                    .map(ExternalUniversityResponseDto::dataSearch)
                    .map(ExternalUniversityResponseDto.DataSearch::content)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(UniversityResponseDto::from)
                    .toList();

        } catch (Exception e) {
            log.error("커리어넷 학교 검색 API 호출 중 오류 발생. keyword: {}", keyword, e);
            throw new ExternalServiceException("대학교 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }
}
