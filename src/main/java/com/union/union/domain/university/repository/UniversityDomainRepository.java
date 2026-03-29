package com.union.union.domain.university.repository;

import com.union.union.domain.university.entity.UniversityDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, Long> {

    boolean existsByDomain(String domain);

    Optional<UniversityDomain> findByDomain(String domain);
}
