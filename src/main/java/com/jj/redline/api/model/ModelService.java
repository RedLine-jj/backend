package com.jj.redline.api.model;

import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.model.ModelResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.enums.ModelType;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModelService {

    private final ModelRepository modelRepository;
    private final SiteOptionRepository siteOptionRepository;

    public CursorPageResponse<ModelResponse> getModels(List<Long> brandIds, List<ModelType> types, Long cursor, int size) {
        List<Model> models = modelRepository.findModelsWithCursor(brandIds, types, cursor, size);
        boolean hasNext = models.size() > size;
        if (hasNext) {
            models = models.subList(0, size);
        }
        Long nextCursor = hasNext ? models.get(models.size() - 1).getId() : null;

        // 최저가 batch 조회
        List<Long> modelIds = models.stream().map(Model::getId).toList();
        Map<Long, Integer> lowestPriceMap = Map.of();
        if (!modelIds.isEmpty()) {
            lowestPriceMap = siteOptionRepository.findMinPriceByModelIds(modelIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (Integer) row[1]
                    ));
        }

        Map<Long, Integer> finalLowestPriceMap = lowestPriceMap;
        List<ModelResponse> content = models.stream()
                .map(m -> new ModelResponse(
                        m.getId(),
                        m.getBrand().getId(),
                        m.getBrand().getBrandName(),
                        m.getBrand().getBrandNameKo(),
                        m.getModelName(),
                        m.getImageUrl(),
                        m.getType(),
                        finalLowestPriceMap.get(m.getId())
                ))
                .toList();
        log.debug("getModels result: count={}, hasNext={}, cursor={}", content.size(), hasNext, nextCursor);
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    public long getModelCount(List<Long> brandIds) {
        if (brandIds != null && !brandIds.isEmpty()) {
            return modelRepository.countByBrandIdIn(brandIds);
        }
        return modelRepository.count();
    }

    public ModelResponse getModel(Long id) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Model not found: " + id));

        Integer lowestPrice = siteOptionRepository.findMinPriceByModelIds(List.of(id))
                .stream()
                .findFirst()
                .map(row -> (Integer) row[1])
                .orElse(null);

        return new ModelResponse(
                model.getId(),
                model.getBrand().getId(),
                model.getBrand().getBrandName(),
                model.getBrand().getBrandNameKo(),
                model.getModelName(),
                model.getImageUrl(),
                model.getType(),
                lowestPrice
        );
    }
}
