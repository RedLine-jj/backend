package com.jj.redline.api.dashboard;

import com.jj.redline.domain.dto.dashboard.OptionPriceItem;
import com.jj.redline.domain.dto.dashboard.PriceComparisonResponse;
import com.jj.redline.domain.dto.dashboard.SiteComparisonItem;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.repository.ModelRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ModelRepository modelRepository;
    private final SiteOptionRepository siteOptionRepository;

    public PriceComparisonResponse getPriceComparison(Long modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found: " + modelId));

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
}
