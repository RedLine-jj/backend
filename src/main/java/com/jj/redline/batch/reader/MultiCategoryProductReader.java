package com.jj.redline.batch.reader;

import com.jj.redline.crawling.modeman.ModeManHttpClient;
import com.jj.redline.crawling.modeman.list.ListParseResult;
import com.jj.redline.crawling.modeman.list.ModeManListParser;
import com.jj.redline.domain.dto.CategoryDto;
import com.jj.redline.domain.dto.ProductBrief;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MultiCategoryProductReader implements ItemReader<ProductBrief>, StepExecutionListener {

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
    public void beforeStep(StepExecution stepExecution) {
        log.info("Initializing MultiCategoryProductReader: Fetching all product briefs...");
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

        String categoryNames = categoriesToCrawl.stream()
                .map(CategoryToCrawl::name)
                .collect(Collectors.joining(", "));
        jobExecutionContext.putString("crawledCategoryNames", categoryNames);

        String categoryCodes = categoriesToCrawl.stream()
                .map(CategoryToCrawl::code)
                .collect(Collectors.joining(", "));
        jobExecutionContext.putString("crawledCategoryCodes", categoryCodes);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // do nothing
        return null;
    }

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
        List<ProductBrief> allProductBriefs = new CopyOnWriteArrayList<>();

        categoriesToCrawl.parallelStream().forEach(category -> {
            int page = 1;
            Set<String> previousPageKeys = new HashSet<>();
            while (true) {
                String listUrl = String.format(LIST_URL_TEMPLATE, category.code(), page);
                try {
                    log.debug("Fetching product list for category: {} (URL: {})", category.name(), listUrl);
                    String html = httpClient.get(listUrl);
                    ListParseResult parseResult = listParser.parse(html);

                    if (parseResult.getProductBriefs().isEmpty()) {
                        log.debug("No more products found for category: {} at page: {}", category.name(), page);
                        break;
                    }

                    Set<String> currentPageKeys = parseResult.getProductBriefs().stream()
                            .map(ProductBrief::getProductKey)
                            .collect(Collectors.toSet());

                    if (currentPageKeys.equals(previousPageKeys)) {
                        log.info("Duplicate page detected for category: {} at page: {}. Stopping.", category.name(), page);
                        break;
                    }
                    previousPageKeys = currentPageKeys;

                    CategoryDto categoryDto = new CategoryDto(Long.parseLong(category.code()), category.name());

                    List<ProductBrief> briefsWithCategory = parseResult.getProductBriefs().stream()
                            .map(brief -> brief.toBuilder().category(categoryDto).build())
                            .collect(Collectors.toList());

                    allProductBriefs.addAll(briefsWithCategory);
                    log.debug("Fetched {} products from URL: {}", briefsWithCategory.size(), listUrl);

                    page++;
                } catch (Exception e) {
                    log.error("Failed to fetch product list for category: {} (URL: {})", category.name(), listUrl, e);
                    break;
                }
            }
        });

        log.info("Total products fetched from all categories: {}", allProductBriefs.size());
        this.productBriefIterator = allProductBriefs.iterator();
    }
}
