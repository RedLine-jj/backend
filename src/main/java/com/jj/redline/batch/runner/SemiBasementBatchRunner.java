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
public class SemiBasementBatchRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job semiBasementCrawlingJob;

    /**
     * 기본 false (서버 켤 때마다 배치 실행되는 사고 방지)
     * 실행하고 싶을 때만:
     *  ./gradlew bootRun --args='--batch.run.semibasement=true'
     */
    @Value("${batch.run.semibasement:false}")
    private boolean run;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!run) {
            log.info("[SemiBasementBatchRunner] skip (batch.run.semibasement=false)");
            return;
        }

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("runId", String.valueOf(Instant.now().toEpochMilli()));

        for (String arg : args.getNonOptionArgs()) {
            if (arg.contains("=")) {
                String[] parts = arg.split("=", 2);
                if (parts.length == 2) {
                    builder.addString(parts[0], parts[1]);
                }
            }
        }

        JobParameters params = builder.toJobParameters();

        log.info("[SemiBasementBatchRunner] start job: {}", semiBasementCrawlingJob.getName());
        JobExecution exec = jobLauncher.run(semiBasementCrawlingJob, params);
        log.info("[SemiBasementBatchRunner] end job: status={}, exitStatus={}",
                exec.getStatus(), exec.getExitStatus().getExitCode());
    }
}
