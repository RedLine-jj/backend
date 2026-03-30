package com.jj.redline.batch.output;

import com.jj.redline.common.ai.AiModelMatcher;
import com.jj.redline.domain.entity.Brand;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.ModelAlias;
import com.jj.redline.domain.entity.ModelType;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.repository.ModelAliasRepository;
import com.jj.redline.domain.repository.ModelRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchModelResolver {

    private final ModelRepository modelRepository;
    private final ModelAliasRepository modelAliasRepository;
    private final AiModelMatcher aiModelMatcher;

    public Model resolve(Brand brand, String modelName, String imageUrl, ModelType modelType, Site site) {
        Optional<Model> exact = modelRepository.findByBrandAndModelName(brand, modelName);
        if (exact.isPresent()) {
            return exact.get();
        }

        Optional<ModelAlias> alias = modelAliasRepository.findByAliasName(modelName);
        if (alias.isPresent() && alias.get().getModel().getBrand().getId().equals(brand.getId())) {
            return alias.get().getModel();
        }

        List<String> existingNames = modelRepository.findByBrandWithOtherSiteOptions(brand, site)
                .stream()
                .map(Model::getModelName)
                .toList();

        if (existingNames.isEmpty()) {
            log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
            return modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
        }

        AiModelMatcher.MatchResult matchResult = aiModelMatcher.findMatch(brand.getBrandName(), modelName, existingNames);

        return switch (matchResult.status()) {
            case MATCHED -> resolveMatchedModel(brand, modelName, imageUrl, modelType, matchResult);
            case NO_MATCH -> {
                log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
                yield modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
            }
            case ERROR -> {
                log.warn("[DbSnapshotWriter] AI unavailable, skipping: modelName={}", modelName);
                yield null;
            }
        };
    }

    private Model resolveMatchedModel(
            Brand brand,
            String modelName,
            String imageUrl,
            ModelType modelType,
            AiModelMatcher.MatchResult matchResult
    ) {
        Optional<Model> matched = modelRepository.findByBrandAndModelName(brand, matchResult.matchedName());
        if (matched.isPresent()) {
            modelAliasRepository.save(ModelAlias.of(modelName, matched.get()));
            log.info("[DbSnapshotWriter] AI model match: '{}' -> '{}' (confidence: {}%)",
                    modelName, matchResult.matchedName(), matchResult.confidence());
            return matched.get();
        }

        log.info("[DbSnapshotWriter] tb_model insert: modelName={}", modelName);
        return modelRepository.save(Model.of(brand, modelName, imageUrl, modelType));
    }
}
