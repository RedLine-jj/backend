package com.jj.redline.batch.output;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class CrawlMeta {
    private String site;                // "MODEMAN"
    private String categoryCode;        // "263" 등 (없으면 null)
    private OffsetDateTime startedAt;   // UTC 권장
    private OffsetDateTime finishedAt;  // UTC 권장

    private int totalProducts;          // 처리 시도 수
    private int okCount;                // parseStatus OK
    private int partialCount;           // parseStatus PARTIAL
    private int failCount;              // parseStatus FAIL

    private int totalOptions;           // 옵션 총합
    private String snapshotFilePath;    // ndjson 경로(문자열)
}