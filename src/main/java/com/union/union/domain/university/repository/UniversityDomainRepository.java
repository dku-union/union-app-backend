package com.union.union.domain.university.repository;

import com.union.union.domain.university.entity.UniversityDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, Long> {
    
    /**
     * 특정 도메인이 DB에 존재하는지 확인합니다.
     * @param domain 확인할 도메인 (예: dankook.ac.kr)
     * @return 존재 여부
     */
    boolean existsByDomain(String domain);
}
