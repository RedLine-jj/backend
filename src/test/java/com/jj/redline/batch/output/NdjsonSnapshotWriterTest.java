package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.common.config.ModeManCrawlProperties;
import com.jj.redline.domain.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NdjsonSnapshotWriterTest {

    @Test
    void write_writes_one_line_ndjson() throws Exception {
        // 1. 의존성 준비
        ObjectMapper om = new ObjectMapper();
        ModeManCrawlProperties props = new ModeManCrawlProperties();
        props.setOutputRootDir("build/test-output");
        props.setSiteDir("modeman");
        String testFileName = "test-snapshot.ndjson";

        // 2. 테스트 대상 생성 (새로운 생성자 사용)
        NdjsonSnapshotWriter writer = new NdjsonSnapshotWriter(om, props, testFileName);

        // 3. 테스트 데이터 준비
        ProductSnapshot snap = ProductSnapshot.builder()
                .site(Site.MODEMAN)
                .category(new CategoryDto(263, "Denim Jackets"))
                .brand("PHIGVEL MAKERS CO.")
                .name("PM-301 Indigo Classic Wide Jeans")
                .url("https://mode-man.com/product/pm-301-indigo-classic-wide-jeans/8128/category/212/display/1/")
                .imageUrl("https://mode-man.com/web/product/big/202208/xxx.jpg")
                .price(378000L)
                .capturedAt(OffsetDateTime.of(2026, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC))
                .options(List.of(
                        ProductOption.builder().optionLabel("1(30)").status(StockStatus.SOLD_OUT).build()
                ))
                .parseStatus(ParseStatus.OK)
                .build();

        // 4. 실행 (새로운 write 메소드 사용)
        // StepScope 동작을 모방하기 위해 beforeStep을 수동 호출
        writer.beforeStep(null);
        writer.write(new Chunk<>(snap));

        // 5. 결과 검증
        String today = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path expectedPath = Path.of(props.getOutputRootDir(), props.getSiteDir(), today, testFileName);
        assertThat(Files.exists(expectedPath)).isTrue();

        String content = Files.readString(expectedPath);
        assertThat(content)
                .contains("\"site\":\"MODEMAN\"")
                .contains("\"options\"")
                .endsWith("\n"); // 줄바꿈으로 끝나는지 확인
    }
}