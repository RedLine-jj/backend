package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jj.redline.common.config.ModeManCrawlProperties;
import com.jj.redline.domain.dto.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class NdjsonSnapshotWriter {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper;
    private final ModeManCrawlProperties props;

    /**
     * @return 실제 기록된 파일 경로
     */
    public Path append(ProductSnapshot snapshot) throws IOException {
        if (snapshot == null) throw new IllegalArgumentException("snapshot is null");

        Path filePath = resolveSnapshotPath(snapshot.getCapturedAt());
        Files.createDirectories(filePath.getParent());

        // OffsetDateTime 직렬화 지원 ObjectMapper
        ObjectMapper om = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 1줄 JSON
        String line = om.writeValueAsString(snapshot);

        try (BufferedWriter w = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        )) {
            w.write(line);
            w.newLine();
        }
        return filePath;
    }

    public Path resolveSnapshotPath(OffsetDateTime capturedAt) {
        String day = utcDay(capturedAt);
        return Paths.get(props.getOutputRootDir(), props.getSiteDir(), day, props.getSnapshotFileName());
    }

    private String utcDay(OffsetDateTime odt) {
        OffsetDateTime base = (odt == null) ? OffsetDateTime.now(ZoneOffset.UTC) : odt;
        return base.withOffsetSameInstant(ZoneOffset.UTC).format(DAY_FMT);
    }
}