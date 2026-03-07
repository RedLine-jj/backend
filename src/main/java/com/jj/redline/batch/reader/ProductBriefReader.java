package com.jj.redline.batch.reader;

import com.jj.redline.domain.dto.ProductBrief;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ExecutionContext;
import java.util.List;

public class ProductBriefReader implements ItemReader<ProductBrief> {

    private List<ProductBrief> productBriefs;
    private int nextProductIndex;

    @BeforeStep
    @SuppressWarnings("unchecked")
    public void retrieveInterstepData(StepExecution stepExecution) {
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        this.productBriefs = (List<ProductBrief>) jobExecutionContext.get("productBriefs");
        this.nextProductIndex = 0;
    }

    @Override
    public ProductBrief read() {
        if (productBriefs == null || nextProductIndex >= productBriefs.size()) {
            return null; // 데이터의 끝을 알림
        }
        ProductBrief nextProduct = productBriefs.get(nextProductIndex);
        nextProductIndex++;
        return nextProduct;
    }
}
