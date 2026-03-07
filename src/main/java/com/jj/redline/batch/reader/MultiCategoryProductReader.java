package com.jj.redline.batch.reader;

import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.crawling.modeman.list.ListParseResult;
import com.jj.redline.crawling.modeman.list.ModeManListParser;
import com.jj.redline.domain.dto.CategoryDto;
import com.jj.redline.domain.dto.ProductBrief;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MultiCategoryProductReader implements ItemReader<ProductBrief> {

    // 크롤링할 카테고리 정보를 담는 내부 클래스
    private record CategoryToCrawl(String code, String name) {}

    private final ModeManHttpClient httpClient;
    private final ModeManListParser listParser;

    // 크롤링할 카테고리 목록
    private final List<CategoryToCrawl> categoriesToCrawl = List.of(
            new CategoryToCrawl("263", "Denim Jackets"),
            new CategoryToCrawl("858", "Denim Pants")
    );

    private static final String LIST_URL_TEMPLATE = "https://mode-man.com/product/list.html?cate_no=%s&page=%d";

    private Iterator<ProductBrief> productBriefIterator;

    @Override
    public ProductBrief read() {
        if (productBriefIterator == null) {
            initialize();
        }

        if (productBriefIterator.hasNext()) {
            return productBriefIterator.next();
        } else {
            return null;
        }
    }

    private void initialize() {
        log.info("Initializing MultiCategoryProductReader: Fetching all product briefs...");
        List<ProductBrief> allProductBriefs = new CopyOnWriteArrayList<>();

        categoriesToCrawl.parallelStream().forEach(category -> {
            int page = 1;
            while (true) {
                String listUrl = String.format(LIST_URL_TEMPLATE, category.code(), page);
                try {
                    log.info("Fetching product list for category: {} (URL: {})", category.name(), listUrl);
                    String html = httpClient.get(listUrl);
                    ListParseResult parseResult = listParser.parse(html);

                    if (parseResult.getProductBriefs().isEmpty()) {
                        log.info("No more products found for category: {} at page: {}", category.name(), page);
                        break; // 더 이상 상품이 없으면 중단
                    }

                    CategoryDto categoryDto = new CategoryDto(Long.parseLong(category.code()), category.name());

                    // 각 ProductBrief 객체에 현재 크롤링 중인 카테고리 정보를 설정합니다.
                    List<ProductBrief> briefsWithCategory = parseResult.getProductBriefs().stream()
                            .map(brief -> brief.toBuilder().category(categoryDto).build())
                            .collect(Collectors.toList());

                    allProductBriefs.addAll(briefsWithCategory);
                    log.info("Fetched {} products from URL: {}", briefsWithCategory.size(), listUrl);

                    page++; // 다음 페이지로 이동
                } catch (Exception e) {
                    log.error("Failed to fetch product list for category: {} (URL: {})", category.name(), listUrl, e);
                    break; // 에러 발생 시 해당 카테고리 페이지네이션 중단
                }
            }
        });

        log.info("Total products fetched from all categories: {}", allProductBriefs.size());
        this.productBriefIterator = allProductBriefs.iterator();
    }
}