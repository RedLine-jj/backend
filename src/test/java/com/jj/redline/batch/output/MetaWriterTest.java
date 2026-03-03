package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jj.redline.common.config.ModeManCrawlProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MetaWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void write_creates_meta_json() throws Exception {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ModeManCrawlProperties props = mock(ModeManCrawlProperties.class);
        when(props.getOutputRootDir()).thenReturn(tempDir.toString());
        when(props.getSiteDir()).thenReturn("modeman");
        when(props.getMetaFileName()).thenReturn("meta.json");

        MetaWriter writer = new MetaWriter(om, props);

        OffsetDateTime started = OffsetDateTime.of(2026, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime finished = started.plusSeconds(3);

        MetaReport report = MetaReport.builder()
                .site("MODEMAN")
                .categoryCode(263)
                .categoryName("Denim Jackets")
                .startedAt(started)
                .finishedAt(finished)
                .durationMs(3000L)
                .total(10)
                .ok(8)
                .partial(1)
                .fail(1)
                .errorSamples(List.of(
                        MetaReport.ErrorSample.builder()
                                .url("https://mode-man.com/product/abc/111/")
                                .reason("NO_PRODUCT_NODE")
                                .build()
                ))
                .build();

        Path out = writer.write(report, started);

        assertThat(Files.exists(out)).isTrue();
        String json = Files.readString(out);

        assertThat(json).contains("\"site\" : \"MODEMAN\"");
        assertThat(json).contains("\"categoryCode\" : 263");
        assertThat(json).contains("\"total\" : 10");
        assertThat(json).contains("NO_PRODUCT_NODE");
    }
}