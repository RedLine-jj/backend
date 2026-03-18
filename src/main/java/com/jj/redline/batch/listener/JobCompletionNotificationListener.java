package com.jj.redline.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final DateTimeFormatter KST_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("===== Job [{}] started =====", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED && jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        String jobName = jobExecution.getJobInstance().getJobName();
        ZonedDateTime startedAt = toKst(jobExecution.getStartTime());
        ZonedDateTime finishedAt = toKst(jobExecution.getEndTime());
        long durationMs = (startedAt != null && finishedAt != null)
                ? Duration.between(startedAt, finishedAt).toMillis() : 0;

        StepExecution step = jobExecution.getStepExecutions().stream().findFirst().orElse(null);
        long totalRead = step != null ? step.getReadCount() : 0;
        long writeCount = step != null ? step.getWriteCount() : 0;
        long skipCount = totalRead - writeCount;

        log.info("===== Job [{}] finished: status={}, read={}, write={}, skip={}, duration={}ms, started={}, finished={} =====",
                jobName, jobExecution.getStatus(), totalRead, writeCount, skipCount, durationMs,
                startedAt != null ? startedAt.format(KST_FMT) : "N/A",
                finishedAt != null ? finishedAt.format(KST_FMT) : "N/A");
    }

    private ZonedDateTime toKst(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(KST_ZONE_ID);
    }
}
