package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.BrandAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandAliasRepository extends JpaRepository<BrandAlias, Long> {

    Optional<BrandAlias> findByAliasName(String aliasName);
}
