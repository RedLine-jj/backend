package com.jj.redline.api.dashboard;

import com.jj.redline.domain.dto.dashboard.*;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import com.jj.redline.exception.NotFoundException;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ModelRepository modelRepository;
    private final SiteOptionRepository siteOptionRepository;
    private final SiteOptionLogRepository siteOptionLogRepository;

    public PriceComparisonResponse getPriceComparison(Long modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new NotFoundException("Model not found: " + modelId));

        List<SiteOption> siteOptions = siteOptionRepository.findAllByModelIdWithSite(modelId);

        List<SiteComparisonItem> sites = siteOptions.stream()
                .collect(Collectors.groupingBy(so -> so.getSite().getSiteName()))
                .entrySet().stream()
                .map(entry -> new SiteComparisonItem(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(so -> new OptionPriceItem(
                                        so.getOptionLabel(),
                                        so.getPrice(),
                                        so.getStatus(),
                                        so.getUrl()
                                ))
                                .toList()
                ))
                .toList();

        log.debug("getPriceComparison: modelId={}, sites={}", modelId, sites.size());
        return new PriceComparisonResponse(model.getModelName(), model.getImageUrl(), sites);
    }

    public PriceHistoryResponse getPriceHistory(Long modelId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<Tuple> rows = siteOptionLogRepository.findDailyAvgPricesByModelId(modelId, startDate);

        // siteName → (date → avgPrice) 그룹핑
        Map<String, LinkedHashMap<String, Integer>> siteMap = new LinkedHashMap<>();
        for (Tuple row : rows) {
            String siteName = row.get(0, String.class);
            java.sql.Date sqlDate = row.get(1, java.sql.Date.class);
            LocalDate date = sqlDate != null ? sqlDate.toLocalDate() : null;
            Double avgPrice = row.get(2, Double.class);

            if (siteName == null || date == null || avgPrice == null) continue;

            siteMap.computeIfAbsent(siteName, k -> new LinkedHashMap<>())
                    .put(date.toString(), avgPrice.intValue());
        }

        List<SitePriceHistory> sites = siteMap.entrySet().stream()
                .map(entry -> {
                    String siteName = entry.getKey();
                    LinkedHashMap<String, Integer> dailyMap = entry.getValue();

                    List<DailyPrice> history = dailyMap.entrySet().stream()
                            .map(e -> new DailyPrice(e.getKey(), e.getValue()))
                            .toList();

                    List<Integer> prices = new ArrayList<>(dailyMap.values());
                    Integer currentPrice = prices.isEmpty() ? null : prices.get(prices.size() - 1);
                    Integer minPrice = prices.stream().min(Integer::compareTo).orElse(null);
                    Integer maxPrice = prices.stream().max(Integer::compareTo).orElse(null);

                    // 가격 변동: 최신 vs 7일전 (또는 가장 오래된 데이터)
                    Integer priceChange = null;
                    if (prices.size() >= 2) {
                        int refIndex = Math.max(0, prices.size() - 8); // ~7일전
                        priceChange = currentPrice - prices.get(refIndex);
                    }

                    return new SitePriceHistory(siteName, currentPrice, priceChange, minPrice, maxPrice, history);
                })
                .toList();

        log.debug("getPriceHistory: modelId={}, days={}, sites={}", modelId, days, sites.size());
        return new PriceHistoryResponse(sites);
    }
}
