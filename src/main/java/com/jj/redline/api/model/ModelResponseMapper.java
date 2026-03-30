package com.jj.redline.api.model;

import com.jj.redline.domain.dto.model.ModelResponse;
import com.jj.redline.domain.entity.Model;
import org.springframework.stereotype.Component;

@Component
public class ModelResponseMapper {

    public ModelResponse toResponse(Model model, Integer lowestPrice) {
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
