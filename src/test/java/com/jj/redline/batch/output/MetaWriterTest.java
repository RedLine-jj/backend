package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jj.redline.common.config.ModeManCrawlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled
class MetaWriterTest {

    @TempDir
    Path tempDir;

    private MetaWriter writer;
    private ModeManCrawlProperties props;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        props = mock(ModeManCrawlProperties.class);
        when(props.getOutputRootDir()).thenReturn(tempDir.toString());
        when(props.getSiteDir()).thenReturn("modeman");
        when(props.getMetaFileName()).thenReturn("meta.ndjson");

        writer = new MetaWriter(om, props);
        testTime = OffsetDateTime.of(2026, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void write_appends_ndjson_lines() throws Exception {
        // 첫 번째 리포트 생성 및 쓰기
        MetaReport report1 = MetaReport.builder()
                .site("MODEMAN")
                .categoryCode(263)
                .startedAt(testTime)
                .finishedAt(testTime.plusSeconds(3))
                .durationMs(3000L)
                .total(10L)
                .ok(8L)
                .partial(1L)
                .fail(1L)
                .build();

        Path out = writer.write(report1, testTime);

        // 두 번째 리포트 생성 및 쓰기
        MetaReport report2 = MetaReport.builder()
                .site("MODEMAN")
                .categoryCode(858)
                .startedAt(testTime.plusMinutes(1))
                .finishedAt(testTime.plusMinutes(1).plusSeconds(5))
                .durationMs(5000L)
                .total(20L)
                .ok(19L)
                .partial(0L)
                .fail(1L)
                .build();

        writer.write(report2, testTime);

        // 결과 검증
        assertThat(Files.exists(out)).isTrue();
        String content = Files.readString(out);

        // 1. 두 줄이 있어야 함 (이어쓰기 확인)
        String[] lines = content.split("\n");
        assertThat(lines).hasSize(2);

        // // 2. 각 줄에 맞는 내용이 포함되어 있는지 확인
        // assertThat(lines[0]).contains("categoryCode":263);
        // assertThat(lines[0]).contains("total":10);
        // assertThat(lines[1]).contains("categoryCode":858);
        // assertThat(lines[1]).contains("total":20);
    }
}