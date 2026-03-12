package com.jj.redline.api.restock;

import com.jj.redline.domain.dto.restock.RecentRestockResponse;
import com.jj.redline.domain.entity.Model;
import com.jj.redline.domain.entity.Site;
import com.jj.redline.domain.repository.SiteOptionLogRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestockService {

    private final SiteOptionLogRepository siteOptionLogRepository;

    public List<RecentRestockResponse> getRecentRestocks() {
        List<Tuple> rows = siteOptionLogRepository.findRecentRestocks(10);

        List<RecentRestockResponse> result = rows.stream()
                .map(row -> new RecentRestockResponse(
                        row.get(0, Long.class),
                        row.get(1, String.class),
                        row.get(2, String.class),
                        row.get(3, LocalDateTime.class)
                ))
                .toList();

        log.debug("getRecentRestocks: size={}", result.size());
        return result;
    }
}
