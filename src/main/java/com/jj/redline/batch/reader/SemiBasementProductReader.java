package com.jj.redline.batch.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.crawling.modeman.list.ListParseResult;
import com.jj.redline.crawling.semibasement.SemiBasementHttpClient;
import com.jj.redline.crawling.semibasement.list.SemiBasementListParser;
import com.jj.redline.domain.dto.CategoryDto;
import com.jj.redline.domain.dto.ProductBrief;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SemiBasementProductReader implements ItemReader<ProductBrief>, StepExecutionListener {

    private record CategoryToCrawl(String code, String name) {}

    private static final String LIST_URL_TEMPLATE = "https://semibasement.com/%s/?page=%d&sort=recent";
    private static final String DETAIL_URL_TEMPLATE = "https://semibasement.com/%s/?idx=%s";

    private final SemiBasementHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final List<CategoryToCrawl> categoriesToCrawl = List.of(
            new CategoryToCrawl("89", "Denim Pants"),
            new CategoryToCrawl("93", "Denim Jackets")
    );

    private SemiBasementListParser listParser;
    private Iterator<ProductBrief> productBriefIterator;

    @PostConstruct
    void initParser() {
        this.listParser = new SemiBasementListParser(objectMapper);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Initializing SemiBasementProductReader: Fetching all product briefs...");
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
        return null;
    }

    @Override
    public ProductBrief read() {
        if (productBriefIterator == null) {
            initialize();
        }

        if (productBriefIterator.hasNext()) {
            return productBriefIterator.next();
        }
        return null;
    }

    private void initialize() {
        List<ProductBrief> allProductBriefs = new CopyOnWriteArrayList<>();

        categoriesToCrawl.parallelStream().forEach(category -> {
            int page = 1;
            while (true) {
                String listUrl = String.format(LIST_URL_TEMPLATE, category.code(), page);
                try {
                    log.info("Fetching product list for category: {} (URL: {})", category.name(), listUrl);
                    String html = httpClient.getHtml(listUrl);
                    ListParseResult parseResult = listParser.parse(html);

                    if (parseResult.getProductBriefs().isEmpty()) {
                        log.info("No more products found for category: {} at page: {}", category.name(), page);
                        break;
                    }

                    CategoryDto categoryDto = new CategoryDto(Long.parseLong(category.code()), category.name());

                    List<ProductBrief> briefsWithCategory = parseResult.getProductBriefs().stream()
                            .map(brief -> brief.toBuilder()
                                    .site("SEMI_BASEMENT")
                                    .url(String.format(DETAIL_URL_TEMPLATE, category.code(), brief.getProductKey()))
                                    .category(categoryDto)
                                    .build())
                            .collect(Collectors.toList());

                    allProductBriefs.addAll(briefsWithCategory);
                    log.info("Fetched {} products from URL: {}", briefsWithCategory.size(), listUrl);
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
