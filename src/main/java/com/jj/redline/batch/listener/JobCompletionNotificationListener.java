package com.jj.redline.batch.listener;

import com.jj.redline.batch.output.MetaReport;
import com.jj.redline.batch.output.MetaWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final DateTimeFormatter FILENAME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter KST_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MetaWriter metaWriter;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(FILENAME_FMT);
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

        // 1. 서버의 시스템 시간대 기준으로 ZonedDateTime 생성
        ZonedDateTime startedAtSystemZone = toZonedDateTime(jobExecution.getStartTime());
        ZonedDateTime finishedAtSystemZone = toZonedDateTime(jobExecution.getEndTime());

        // 2. UTC 시간과 한국 시간으로 각각 변환
        OffsetDateTime startedAtUtc = toUtc(startedAtSystemZone);
        OffsetDateTime finishedAtUtc = toUtc(finishedAtSystemZone);
        String krStartedAt = toKstString(startedAtSystemZone);
        String krFinishedAt = toKstString(finishedAtSystemZone);

        long durationMs = (startedAtUtc != null && finishedAtUtc != null) ? Duration.between(startedAtUtc, finishedAtUtc).toMillis() : 0;

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

        ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
        String categoryNames = jobExecutionContext.getString("crawledCategoryNames", "ALL");
        String categoryCodes = jobExecutionContext.getString("crawledCategoryCodes", "");


        MetaReport report = MetaReport.builder()
                .site("MODEMAN")
                .categoryName(categoryNames)
                .categoryCode(categoryCodes)
                .startedAt(startedAtUtc)
                .krStartedAt(krStartedAt)
                .finishedAt(finishedAtUtc)
                .krFinishedAt(krFinishedAt)
                .durationMs(durationMs)
                .total(totalRead)
                .ok(okCount)
                .partial(0L)
                .fail(failCount)
                .errorSamples(errorSamples)
                .build();

        try {
            metaWriter.write(report, startedAtUtc);
            log.info("===== Meta report generated successfully. =====");
        } catch (IOException e) {
            log.error("Failed to write meta report", e);
        }
    }

    private ZonedDateTime toZonedDateTime(LocalDateTime ldt) {
        if (ldt == null) return null;
        // 서버의 기본 시간대를 사용하여 ZonedDateTime으로 변환
        return ldt.atZone(ZoneId.systemDefault());
    }

    private OffsetDateTime toUtc(ZonedDateTime zdt) {
        if (zdt == null) return null;
        // UTC 시간으로 변환
        return zdt.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }

    private String toKstString(ZonedDateTime zdt) {
        if (zdt == null) return null;
        // 한국 시간으로 변환하고 포맷에 맞게 문자열로 반환
        return zdt.withZoneSameInstant(KST_ZONE_ID).format(KST_FMT);
    }
}