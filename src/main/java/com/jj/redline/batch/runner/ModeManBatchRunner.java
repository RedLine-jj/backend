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

        JobParameters params = new JobParametersBuilder()
                .addString("runId", String.valueOf(Instant.now().toEpochMilli()))
                .toJobParameters();

        log.info("[ModeManBatchRunner] start job: {}", modeManCrawlingJob.getName());
        JobExecution exec = jobLauncher.run(modeManCrawlingJob, params);
        log.info("[ModeManBatchRunner] end job: status={}, exitStatus={}",
                exec.getStatus(), exec.getExitStatus().getExitCode());
    }
}