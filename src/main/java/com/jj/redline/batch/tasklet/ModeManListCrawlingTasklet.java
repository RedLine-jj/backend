package com.jj.redline.batch.tasklet;

import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.crawling.modeman.list.ModeManListParser;
import com.jj.redline.domain.dto.ProductBrief;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ModeManListCrawlingTasklet implements Tasklet {

    private final ModeManHttpClient httpClient;
    private final ModeManListParser listParser;

    private static final String LIST_URL =
            "https://mode-man.com/product/list.html?cate_no=263";

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {

        System.out.println("===== ModeMan LIST CRAWLING START =====");

        // 1. HTTP 호출
        String html = httpClient.get(LIST_URL);

        // 2. 리스트 파싱
        List<ProductBrief> products = listParser.parse(html);

        System.out.println("수집 상품 수 = " + products.size());

        // 3. 로그 출력 (테스트용)
        products.stream().limit(5).forEach(p -> {
            System.out.println("--------------------------------");
            System.out.println("NAME  : " + p.getName());
            System.out.println("BRAND : " + p.getBrand());
            System.out.println("PRICE : " + p.getPrice());
            System.out.println("URL   : " + p.getUrl());
        });

        System.out.println("===== ModeMan LIST CRAWLING END =====");

        return RepeatStatus.FINISHED;
    }
}