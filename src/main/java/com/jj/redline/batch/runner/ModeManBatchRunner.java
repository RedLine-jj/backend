package com.jj.redline.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModeManBatchRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job modeManCrawlingJob;

    /**
     * 기본 false (서버 켤 때마다 배치 실행되는 사고 방지)
     * 실행하고 싶을 때만:
     *  ./gradlew bootRun --args='--run.modeManBatch=true'
     */
    @Value("${batch.run.modeman:false}")
    private boolean run;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!run) {
            log.info("[ModeManBatchRunner] skip (batch.run.modeman=false)");
            return;
        }

        JobParametersBuilder builder = new JobParametersBuilder();

        // Job을 재실행할 수 있도록 항상 고유한 파라미터를 추가합니다.
        builder.addString("runId", String.valueOf(Instant.now().toEpochMilli()));

        // 커맨드 라인 인자에서 key=value 형식의 파라미터를 찾아 추가합니다.
        for (String arg : args.getNonOptionArgs()) {
            if (arg.contains("=")) {
                String[] parts = arg.split("=", 2);
                if (parts.length == 2) {
                    // categoryCode, categoryName 등을 Job 파라미터로 추가
                    builder.addString(parts[0], parts[1]);
                }
            }
        }

        JobParameters params = builder.toJobParameters();

        log.info("[ModeManBatchRunner] start job: {}", modeManCrawlingJob.getName());
        JobExecution exec = jobLauncher.run(modeManCrawlingJob, params);
        log.info("[ModeManBatchRunner] end job: status={}, exitStatus={}",
                exec.getStatus(), exec.getExitStatus().getExitCode());
    }
}