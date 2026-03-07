package com.jj.redline.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redline.crawl")
public class ModeManCrawlProperties {

    /**
     * 출력 루트 디렉토리 (상대/절대 모두 가능)
     * 예: output
     */
    private String outputRootDir = "output";

    /**
     * 사이트별 하위 디렉토리
     * 예: modeman
     */
    private String siteDir = "modeman";

    /**
     * 스냅샷 파일명
     */
    private String snapshotFileName = "snapshot.ndjson";

    /**
     * 메타 파일명
     */
    private String metaFileName = "meta.ndjson";
}