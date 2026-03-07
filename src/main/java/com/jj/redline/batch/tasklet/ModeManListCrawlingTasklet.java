package com.jj.redline.batch.tasklet;

import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.crawling.modeman.list.ModeManListParser;
import com.jj.redline.domain.dto.ProductBrief;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope // JobParameter를 주입받기 위해 StepScope로 지정
@RequiredArgsConstructor
public class ModeManListCrawlingTasklet implements Tasklet {

    private final ModeManHttpClient httpClient;
    private final ModeManListParser listParser;

    // Job Parameter에서 카테고리 코드를 주입받습니다.
    @Value("#{jobParameters['categoryCode']}")
    private long categoryCode;

    private static final String LIST_URL_TEMPLATE =
            "https://mode-man.com/product/list.html?cate_no=%d";

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {

        System.out.println("===== ModeMan LIST CRAWLING START (cate_no=" + categoryCode + ") =====");

        // 1. HTTP 호출 (URL 동적 생성)
        String listUrl = String.format(LIST_URL_TEMPLATE, categoryCode);
        String html = httpClient.get(listUrl);

        // 2. 리스트 파싱
        List<ProductBrief> products = listParser.parse(html);

        System.out.println("수집 상품 수 = " + products.size());

        // 3. [수정됨] 다음 스텝으로 전달하기 위해 ExecutionContext에 데이터 저장
        contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("productBriefs", products);

        System.out.println("===== ModeMan LIST CRAWLING END =====");

        return RepeatStatus.FINISHED;
    }
}
