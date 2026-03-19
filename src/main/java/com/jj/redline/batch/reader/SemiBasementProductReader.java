package com.jj.redline.batch.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.domain.dto.crawl.ListParseResult;
import com.jj.redline.crawling.imweb.ImwebHttpClient;
import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.dto.crawl.CrawlSiteConfig.CategoryToCrawl;
import com.jj.redline.crawling.imweb.ImwebListParser;
import com.jj.redline.domain.dto.crawl.CategoryDto;
import com.jj.redline.domain.dto.crawl.ProductBrief;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SemiBasementProductReader implements ItemReader<ProductBrief>, StepExecutionListener {

    private final ImwebHttpClient httpClient;
    private final CrawlSiteConfig siteConfig;
    private final ObjectMapper objectMapper;

    private ImwebListParser listParser;
    private Iterator<ProductBrief> productBriefIterator;

    @PostConstruct
    void initParser() {
        this.listParser = new ImwebListParser(objectMapper);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Initializing SemiBasementProductReader: Fetching all product briefs...");
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

        List<CategoryToCrawl> categories = siteConfig.getCategories();

        String categoryNames = categories.stream()
                .map(CategoryToCrawl::name)
                .collect(Collectors.joining(", "));
        jobExecutionContext.putString("crawledCategoryNames", categoryNames);

        String categoryCodes = categories.stream()
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
        String listUrlTemplate = siteConfig.getListUrlTemplate();
        String detailUrlTemplate = siteConfig.getDetailUrlTemplate();
        String siteName = siteConfig.getSite().name();

        siteConfig.getCategories().parallelStream().forEach(category -> {
            int page = 1;
            Set<String> previousPageKeys = new HashSet<>();
            while (true) {
                String listUrl = String.format(listUrlTemplate, category.code(), page);
                try {
                    log.debug("Fetching product list for category: {} (URL: {})", category.name(), listUrl);
                    String html = httpClient.getHtml(listUrl);
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
                            .map(brief -> brief.toBuilder()
                                    .site(siteName)
                                    .url(String.format(detailUrlTemplate, category.code(), brief.getProductKey()))
                                    .category(categoryDto)
                                    .build())
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
