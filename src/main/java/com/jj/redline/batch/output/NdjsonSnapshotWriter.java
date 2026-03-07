package com.jj.redline.batch.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jj.redline.common.config.ModeManCrawlProperties;
import com.jj.redline.domain.dto.ProductSnapshot;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

@StepScope
public class NdjsonSnapshotWriter implements ItemWriter<ProductSnapshot> {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper;
    private final ModeManCrawlProperties props;
    private final String snapshotFileName;
    private Path filePath;

    // 생성자를 통해 필요한 의존성과 @Value 값을 주입받습니다.
    public NdjsonSnapshotWriter(
            ObjectMapper objectMapper,
            ModeManCrawlProperties props,
            @Value("#{jobExecutionContext['snapshotFileName']}") String snapshotFileName) {
        this.objectMapper = objectMapper;
        this.props = props;
        this.snapshotFileName = snapshotFileName;
    }

    // 스텝이 시작되기 전에 파일 경로를 설정합니다.
    @BeforeStep
    public void beforeStep(final StepExecution stepExecution) throws IOException {
        String day = utcDay(OffsetDateTime.now(ZoneOffset.UTC));
        this.filePath = Paths.get(props.getOutputRootDir(), props.getSiteDir(), day, this.snapshotFileName);
        Files.createDirectories(filePath.getParent());
    }

    @Override
    public void write(Chunk<? extends ProductSnapshot> chunk) throws Exception {
        ObjectMapper om = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 각 청크를 파일에 이어씁니다.
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)
        ) {
            for (ProductSnapshot snapshot : chunk.getItems()) {
                String line = om.writeValueAsString(snapshot);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String utcDay(OffsetDateTime odt) {
        OffsetDateTime base = (odt == null) ? OffsetDateTime.now(ZoneOffset.UTC) : odt;
        return base.withOffsetSameInstant(ZoneOffset.UTC).format(DAY_FMT);
    }
}