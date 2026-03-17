package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.ModelAlias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelAliasRepository extends JpaRepository<ModelAlias, Long> {

    Optional<ModelAlias> findByAliasName(String aliasName);
}
