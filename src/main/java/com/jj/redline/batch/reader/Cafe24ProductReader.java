package com.jj.redline.batch.reader;

import com.jj.redline.crawling.cafe24.Cafe24HttpClient;
import com.jj.redline.crawling.cafe24.Cafe24ListParser;
import com.jj.redline.domain.dto.crawl.CrawlSiteConfig;
import com.jj.redline.domain.dto.crawl.ListParseResult;
import com.jj.redline.domain.dto.crawl.CategoryDto;
import com.jj.redline.domain.dto.crawl.ProductBrief;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class Cafe24ProductReader implements ItemReader<ProductBrief>, StepExecutionListener {

    private final CrawlSiteConfig siteConfig;
    private final Cafe24HttpClient httpClient;
    private final Cafe24ListParser listParser;

    private Iterator<ProductBrief> productBriefIterator;

    public Cafe24ProductReader(CrawlSiteConfig siteConfig, Cafe24HttpClient httpClient, Cafe24ListParser listParser) {
        this.siteConfig = siteConfig;
        this.httpClient = httpClient;
        this.listParser = listParser;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Initializing Cafe24ProductReader: Fetching all product briefs for {}...", siteConfig.getSite());
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

        String categoryNames = siteConfig.getCategories().stream()
                .map(CrawlSiteConfig.CategoryToCrawl::name)
                .collect(Collectors.joining(", "));
        jobExecutionContext.putString("crawledCategoryNames", categoryNames);

        String categoryCodes = siteConfig.getCategories().stream()
                .map(CrawlSiteConfig.CategoryToCrawl::code)
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

        siteConfig.getCategories().parallelStream().forEach(category -> {
            int page = 1;
            Set<String> previousPageKeys = new HashSet<>();
            while (true) {
                String listUrl = String.format(siteConfig.getListUrlTemplate(), category.code(), page);
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

        log.info("Total products fetched from all categories for {}: {}", siteConfig.getSite(), allProductBriefs.size());
        this.productBriefIterator = allProductBriefs.iterator();
    }
}
