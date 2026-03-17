package com.jj.redline.api.site;

import com.jj.redline.domain.dto.site.SiteResponse;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;

    @Cacheable(value = "sites")
    public List<SiteResponse> getSites() {
        List<SiteResponse> result = siteRepository.findAllByOrderBySiteNameAsc().stream()
                .map(site -> new SiteResponse(site.getId(), site.getSiteName(), site.getSiteLink()))
                .toList();
        log.debug("getSites result: count={}", result.size());
        return result;
    }
}
