package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.CrawlSite;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.entity.ModelType;
import org.springframework.stereotype.Component;

@Component
public class BatchModelTypeResolver {

    public ModelType resolve(ProductSnapshot snapshot) {
        if (snapshot.getCategory() == null || snapshot.getSite() == null) {
            return null;
        }

        int categoryCode = (int) snapshot.getCategory().getCode();
        CrawlSite site = snapshot.getSite();

        return switch (site) {
            case MODEMAN -> switch (categoryCode) {
                case 263 -> ModelType.DENIM_JACKET;
                case 858 -> ModelType.DENIM_PANTS;
                default -> null;
            };
            case SEMI_BASEMENT -> switch (categoryCode) {
                case 89 -> ModelType.DENIM_PANTS;
                case 93 -> ModelType.DENIM_JACKET;
                default -> null;
            };
        };
    }
}
