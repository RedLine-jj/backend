package com.jj.redline.batch.output;

import com.jj.redline.domain.dto.ProductOption;
import com.jj.redline.domain.dto.ProductSnapshot;
import com.jj.redline.domain.dto.StockStatus;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SiteOptionPersistenceService {

    private final SiteOptionRepository siteOptionRepository;
    private final SiteOptionLogRepository siteOptionLogRepository;

    public SiteOptionUpdateResult upsert(ProductSnapshot snapshot, Site site, Model model) {
        List<ProductOption> options = snapshot.getOptions();
        if (options == null || options.isEmpty()) {
            return new SiteOptionUpdateResult(false, 0);
        }

        LocalDateTime capturedAt = snapshot.getCapturedAt() != null
                ? snapshot.getCapturedAt().toLocalDateTime()
                : LocalDateTime.now();
        Integer price = snapshot.getPrice() != null ? snapshot.getPrice().intValue() : null;

        boolean restocked = false;
        for (ProductOption option : options) {
            Boolean status = option.getStatus() == StockStatus.AVAILABLE;
            SiteOption siteOption = siteOptionRepository
                    .findBySiteAndModelAndOptionLabel(site, model, option.getOptionLabel())
                    .orElse(null);

            if (siteOption == null) {
                siteOption = siteOptionRepository.save(
                        SiteOption.of(site, model, option.getOptionLabel(),
                                price, snapshot.getUrl(), status, capturedAt));
                siteOptionLogRepository.save(
                        SiteOptionLog.of(siteOption, price, status, capturedAt));
                continue;
            }

            boolean changed = !Objects.equals(siteOption.getPrice(), price)
                    || !Objects.equals(siteOption.getStatus(), status);
            if (changed) {
                log.info("[DbSnapshotWriter] 변경 감지: option={}, price={}→{}, status={}→{}",
                        option.getOptionLabel(), siteOption.getPrice(), price,
                        siteOption.getStatus(), status);
                siteOptionLogRepository.save(
                        SiteOptionLog.of(siteOption, price, status, capturedAt));

                if (!Boolean.TRUE.equals(siteOption.getStatus()) && Boolean.TRUE.equals(status)) {
                    restocked = true;
                }
            }
            siteOption.update(price, status, capturedAt);
        }

        return new SiteOptionUpdateResult(restocked, options.size());
    }

    public record SiteOptionUpdateResult(boolean restocked, int optionCount) {
    }
}
