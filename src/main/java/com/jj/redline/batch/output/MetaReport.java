package com.jj.redline.batch.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaReport {

    private String site; // "MODEMAN"
    private Integer categoryCode;
    private String categoryName;

    private OffsetDateTime startedAt;   // UTC 권장
    private OffsetDateTime finishedAt;  // UTC 권장
    private Long durationMs;

    private Long total;
    private Long ok;
    private Long partial;
    private Long fail;

    // 실패/부분실패 샘플(너무 길어지면 앞에서 N개만 담는 용도)
    private List<ErrorSample> errorSamples;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorSample {
        private String url;
        private String reason; // ex) NO_PRODUCT_NODE, NO_OFFERS ...
    }
}