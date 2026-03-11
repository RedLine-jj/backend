package com.jj.redline.domain.repository;

import com.jj.redline.domain.entity.Model;

import com.jj.redline.domain.entity.ModelType;

import java.util.List;

public interface ModelRepositoryCustom {

    List<Model> findModelsWithCursor(List<Long> brandIds, List<ModelType> types, Long cursor, int size);
}
