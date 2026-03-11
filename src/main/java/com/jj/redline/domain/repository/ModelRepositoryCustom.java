package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;

import java.util.List;

public interface ModelRepositoryCustom {

    List<Model> findModelsWithCursor(List<Long> brandIds, Long cursor, int size);
}
