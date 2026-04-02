package com.union.union.domain.miniapp.repository;

import com.union.union.domain.miniapp.entity.MiniAppCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MiniAppCategoryRepository extends JpaRepository<MiniAppCategory, Long> {

    List<MiniAppCategory> findByIsActiveTrue();

    Optional<MiniAppCategory> findByName(String name);
}
