package com.jj.redline.batch.listener;

import com.jj.redline.batch.output.MetaReport;
import com.jj.redline.batch.output.MetaWriter;
import com.jj.redline.domain.dto.ProductBrief;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final MetaWriter metaWriter;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED && jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        log.info("===== Job is finished. Generating meta report... =====");

        // Job 파라미터에서 카테고리 정보 가져오기
        Integer categoryCode = getIntParam(jobExecution, "categoryCode");
        String categoryName = jobExecution.getJobParameters().getString("categoryName");

        // Job 실행 시간 정보
        OffsetDateTime startedAt = toOffsetDateTime(jobExecution.getStartTime());
        OffsetDateTime finishedAt = toOffsetDateTime(jobExecution.getEndTime());
        long durationMs = Duration.between(startedAt, finishedAt).toMillis();

        // Step 실행 결과에서 통계 정보 집계
        List<ProductBrief> briefList = (List<ProductBrief>) jobExecution.getExecutionContext().get("productBriefs");
        int total = (briefList != null) ? briefList.size() : 0;

        // 상세 처리 스텝의 통계를 가져옵니다.
        StepExecution detailStepExecution = jobExecution.getStepExecutions().stream()
                .filter(se -> se.getStepName().equals("modeManDetailProcessingStep"))
                .findFirst().orElse(null);

        int writeCount = 0;
        if (detailStepExecution != null) {
            writeCount = detailStepExecution.getWriteCount();
        }

        // TODO: 정확한 PARTIAL/FAIL 카운트를 위해서는 ItemProcessor/Writer 레벨의 리스너가 필요합니다.
        // 현재는 전체 읽기 시도 건수와 최종 쓰기 건수의 차이를 실패로 간주하여 근사치를 계산합니다.
        int okCount = writeCount;
        int failCount = total - okCount;

        // 실패 샘플 수집
        List<MetaReport.ErrorSample> errorSamples = new ArrayList<>();
        if (detailStepExecution != null) {
            detailStepExecution.getFailureExceptions().forEach(e -> {
                // 실제로는 예외 내용에서 원인을 파싱하는 로직이 더 필요합니다.
                errorSamples.add(MetaReport.ErrorSample.builder()
                        .url(null) // 현재 구조에서는 실패한 아이템의 URL을 특정하기 어려움
                        .reason(e.getMessage())
                        .build());
            });
        }

        // 최종 리포트 객체 생성
        MetaReport report = MetaReport.builder()
                .site("MODEMAN")
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .durationMs(durationMs)
                .total(total)
                .ok(okCount)
                .partial(0) // TODO: PARTIAL 상태 집계 로직 필요
                .fail(failCount)
                .errorSamples(errorSamples)
                .build();

        try {
            // MetaWriter를 사용해 meta.json 파일 작성
            metaWriter.write(report, startedAt);
            log.info("===== Meta report generated successfully. =====");
        } catch (IOException e) {
            log.error("Failed to write meta report", e);
        }
    }

    private Integer getIntParam(JobExecution jobExecution, String key) {
        String value = jobExecution.getJobParameters().getString(key);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OffsetDateTime toOffsetDateTime(java.util.Date date) {
        if (date == null) return null;
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }
}
