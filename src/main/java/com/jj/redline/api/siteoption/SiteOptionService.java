package com.jj.redline.api.siteoption;

import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionDetailResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionLogResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionResponse;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteOptionService {

    private final SiteOptionRepository siteOptionRepository;
    private final SiteOptionLogRepository siteOptionLogRepository;

    public CursorPageResponse<SiteOptionResponse> getSiteOptions(Long siteId, Long modelId, Boolean status, Long cursor, int size) {
        List<SiteOption> items = siteOptionRepository.findSiteOptionsWithCursor(siteId, modelId, status, cursor, size);
        boolean hasNext = items.size() > size;
        if (hasNext) {
            items = items.subList(0, size);
        }
        Long nextCursor = hasNext ? items.get(items.size() - 1).getId() : null;
        List<SiteOptionResponse> content = items.stream()
                .map(so -> new SiteOptionResponse(
                        so.getId(),
                        so.getSite().getSiteName(),
                        so.getModel().getModelName(),
                        so.getOptionLabel(),
                        so.getPrice(),
                        so.getStatus(),
                        so.getLastCapturedAt()
                ))
                .toList();
        log.debug("getSiteOptions result: count={}, hasNext={}, cursor={}", content.size(), hasNext, nextCursor);
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    public SiteOptionDetailResponse getSiteOption(Long id) {
        SiteOption so = siteOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SiteOption not found: " + id));
        SiteOptionDetailResponse response = new SiteOptionDetailResponse(
                so.getId(),
                so.getSite().getSiteName(),
                so.getSite().getSiteLink(),
                so.getModel().getBrand().getBrandName(),
                so.getModel().getModelName(),
                so.getModel().getImageUrl(),
                so.getOptionLabel(),
                so.getPrice(),
                so.getStatus(),
                so.getUrl(),
                so.getLastCapturedAt()
        );
        log.debug("getSiteOption result: id={}", response.getId());
        return response;
    }

    public CursorPageResponse<SiteOptionLogResponse> getSiteOptionLogs(Long id, Long cursor, int size) {
        List<SiteOptionLog> items = siteOptionLogRepository.findLogsWithCursor(id, cursor, size);
        boolean hasNext = items.size() > size;
        if (hasNext) {
            items = items.subList(0, size);
        }
        Long nextCursor = hasNext ? items.get(items.size() - 1).getId() : null;
        List<SiteOptionLogResponse> content = items.stream()
                .map(l -> new SiteOptionLogResponse(
                        l.getId(),
                        l.getOptionLabel(),
                        l.getPrice(),
                        l.getStatus(),
                        l.getCapturedAt()
                ))
                .toList();
        log.debug("getSiteOptionLogs result: count={}, hasNext={}, cursor={}", content.size(), hasNext, nextCursor);
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }
}
