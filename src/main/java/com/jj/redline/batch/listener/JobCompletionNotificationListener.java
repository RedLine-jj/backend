package com.jj.redline.batch.listener;

import com.jj.redline.batch.output.MetaReport;
import com.jj.redline.batch.output.MetaWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final DateTimeFormatter FILENAME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private final MetaWriter metaWriter;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String timestamp = LocalDateTime.now().format(FILENAME_FMT);
        String snapshotFileName = String.format("snapshot-%s.ndjson", timestamp);
        jobExecution.getExecutionContext().putString("snapshotFileName", snapshotFileName);
        log.info("===== Job started. Snapshot file will be: {} =====", snapshotFileName);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED && jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        log.info("===== Job is finished. Generating meta report... =====");

        OffsetDateTime startedAt = toOffsetUtc(jobExecution.getStartTime());
        OffsetDateTime finishedAt = toOffsetUtc(jobExecution.getEndTime());
        long durationMs = (startedAt != null && finishedAt != null) ? Duration.between(startedAt, finishedAt).toMillis() : 0;

        StepExecution crawlingStepExecution = jobExecution.getStepExecutions().stream()
                .filter(se -> se.getStepName().equals("crawlingStep"))
                .findFirst().orElse(null);

        long totalRead = 0;
        long writeCount = 0;
        if (crawlingStepExecution != null) {
            totalRead = crawlingStepExecution.getReadCount();
            writeCount = crawlingStepExecution.getWriteCount();
        }

        long okCount = writeCount;
        long failCount = totalRead - writeCount;

        List<MetaReport.ErrorSample> errorSamples = new ArrayList<>();
        if (crawlingStepExecution != null) {
            crawlingStepExecution.getFailureExceptions().forEach(e -> {
                errorSamples.add(MetaReport.ErrorSample.builder()
                        .url(null)
                        .reason(e.getMessage())
                        .build());
            });
        }

        MetaReport report = MetaReport.builder()
                .site("MODEMAN")
                .categoryName("ALL") // 모든 카테고리를 포함하므로 "ALL"로 변경
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .durationMs(durationMs)
                .total(totalRead)
                .ok(okCount)
                .partial(0L)
                .fail(failCount)
                .errorSamples(errorSamples)
                .build();

        try {
            metaWriter.write(report, startedAt);
            log.info("===== Meta report generated successfully. =====");
        } catch (IOException e) {
            log.error("Failed to write meta report", e);
        }
    }

    private OffsetDateTime toOffsetUtc(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atOffset(ZoneOffset.UTC);
    }
}