package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;

import java.util.List;

public interface ModelRepositoryCustom {

    List<Model> findModelsWithCursor(Long brandId, Long cursor, int size);
}
