package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.ProductSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NdjsonImportWriter implements ItemWriter<ProductSnapshot> {

    @Override
    public void write(Chunk<? extends ProductSnapshot> chunk) throws Exception {
        for (ProductSnapshot snapshot : chunk.getItems()) {
            log.info("[NdjsonImportWriter] site={}, brand={}, name={}",
                    snapshot.getSite(),
                    snapshot.getBrand(),
                    snapshot.getName());
        }
    }
}
