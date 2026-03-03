package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.common.config.ModeManCrawlProperties;
import com.jj.redline.domain.dto.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NdjsonSnapshotWriterTest {

    @Test
    void append_writes_one_line_ndjson() throws Exception {
        ObjectMapper om = new ObjectMapper();

        ModeManCrawlProperties props = new ModeManCrawlProperties();
        props.setOutputRootDir("build/test-output"); // 테스트 전용
        props.setSiteDir("modeman");
        props.setSnapshotFileName("snapshot.ndjson");
        props.setMetaFileName("meta.json");

        NdjsonSnapshotWriter writer = new NdjsonSnapshotWriter(om, props);

        ProductSnapshot snap = ProductSnapshot.builder()
                .site(Site.MODEMAN)
                .category(new CategoryDto(263, "Denim Jackets"))
                .brand("PHIGVEL MAKERS CO.")
                .name("PM-301 Indigo Classic Wide Jeans")
                .url("https://mode-man.com/product/pm-301-indigo-classic-wide-jeans/8128/category/212/display/1/")
                .imageUrl("https://mode-man.com/web/product/big/202208/xxx.jpg")
                .price(378000)
                .capturedAt(OffsetDateTime.of(2026,3,1,12,0,0,0, ZoneOffset.UTC))
                .options(List.of(
                        ProductOption.builder().optionLabel("1(30)").status(StockStatus.SOLD_OUT).build()
                ))
                .parseMessage(null)
                .build();

        Path file = writer.append(snap);

        assertThat(Files.exists(file)).isTrue();

        String content = Files.readString(file);
        // 1줄짜리 JSON이 들어갔는지 대충 확인
        assertThat(content).contains("\"site\":\"MODEMAN\"");
        assertThat(content).contains("\"options\"");
    }
}