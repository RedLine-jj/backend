package com.jj.redline.api.siteoption;

import com.jj.redline.common.pagination.CursorPaginationSupport;
import com.jj.redline.domain.dto.common.CursorPageResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionDetailResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionLogResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionResponse;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.jj.redline.domain.repository.SiteOptionRepository;
import com.jj.redline.exception.NotFoundException;
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
    private final SiteOptionResponseMapper siteOptionResponseMapper;

    public CursorPageResponse<SiteOptionResponse> getSiteOptions(Long siteId, Long modelId, Boolean status, Long cursor, int size) {
        List<SiteOption> items = siteOptionRepository.findSiteOptionsWithCursor(siteId, modelId, status, cursor, size);
        CursorPaginationSupport.CursorSlice<SiteOption> slice =
                CursorPaginationSupport.slice(items, size, SiteOption::getId);
        List<SiteOptionResponse> content = slice.content().stream()
                .map(siteOptionResponseMapper::toResponse)
                .toList();
        log.debug("getSiteOptions result: count={}, hasNext={}, cursor={}", content.size(), slice.hasNext(), slice.nextCursor());
        return new CursorPageResponse<>(content, slice.nextCursor(), slice.hasNext());
    }

    public SiteOptionDetailResponse getSiteOption(Long id) {
        SiteOption so = siteOptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SiteOption not found: " + id));
        SiteOptionDetailResponse response = siteOptionResponseMapper.toDetailResponse(so);
        log.debug("getSiteOption result: id={}", response.getId());
        return response;
    }

    public CursorPageResponse<SiteOptionLogResponse> getSiteOptionLogs(Long id, Long cursor, int size) {
        List<SiteOptionLog> items = siteOptionLogRepository.findLogsWithCursor(id, cursor, size);
        CursorPaginationSupport.CursorSlice<SiteOptionLog> slice =
                CursorPaginationSupport.slice(items, size, SiteOptionLog::getId);
        List<SiteOptionLogResponse> content = slice.content().stream()
                .map(siteOptionResponseMapper::toLogResponse)
                .toList();
        log.debug("getSiteOptionLogs result: count={}, hasNext={}, cursor={}", content.size(), slice.hasNext(), slice.nextCursor());
        return new CursorPageResponse<>(content, slice.nextCursor(), slice.hasNext());
    }
}
