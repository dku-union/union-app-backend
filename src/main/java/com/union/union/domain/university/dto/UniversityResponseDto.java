package com.union.union.domain.university.dto;

import com.union.union.domain.university.dto.ExternalUniversityResponseDto.Content;

/**
 * 우리 서비스의 클라이언트(API)에게 반환할 커스텀 규격 DTO
 */
public record UniversityResponseDto(
        String schoolName, 
        String address, 
        String link) {
        
    public static UniversityResponseDto from(Content content) {
        return new UniversityResponseDto(
            content.schoolName(),
            content.address(),
            content.link()
        );
    }
}
