package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.redline.common.config.ModeManCrawlProperties;
import java.io.BufferedWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class MetaWriter {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper;
    private final ModeManCrawlProperties props;

    /**
     * @param report meta.json에 저장할 리포트
     * @param capturedAtDay 기준 일자(UTC day). null이면 now(UTC) 사용.
     * @return 실제 기록된 meta.json 경로
     */
    public Path write(MetaReport report, OffsetDateTime capturedAtDay) throws IOException {
        if (report == null) throw new IllegalArgumentException("report is null");

        Path filePath = resolveMetaPath(capturedAtDay);
        Files.createDirectories(filePath.getParent());

        // NDJSON 형식을 위해 pretty print 없이 한 줄로 직렬화
        String jsonLine = objectMapper.writeValueAsString(report);

        // 파일 끝에 한 줄 추가 (APPEND)
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)
        ) {
            writer.write(jsonLine);
            writer.newLine();
        }

        return filePath;
    }

    public Path resolveMetaPath(OffsetDateTime capturedAt) {
        String day = utcDay(capturedAt);
        return Paths.get(props.getOutputRootDir(), props.getSiteDir(), day, props.getMetaFileName());
    }

    private String utcDay(OffsetDateTime odt) {
        OffsetDateTime base = (odt == null) ? OffsetDateTime.now(ZoneOffset.UTC) : odt;
        return base.withOffsetSameInstant(ZoneOffset.UTC).format(DAY_FMT);
    }
}