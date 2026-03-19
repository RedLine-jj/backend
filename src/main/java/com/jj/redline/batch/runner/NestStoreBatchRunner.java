package com.jj.redline.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NestStoreBatchRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job nestStoreCrawlingJob;

    @Value("${batch.run.neststore:false}")
    private boolean run;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!run) {
            log.info("[NestStoreBatchRunner] skip (batch.run.neststore=false)");
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

        log.info("[NestStoreBatchRunner] start job: {}", nestStoreCrawlingJob.getName());
        JobExecution exec = jobLauncher.run(nestStoreCrawlingJob, params);
        log.info("[NestStoreBatchRunner] end job: status={}, exitStatus={}",
                exec.getStatus(), exec.getExitStatus().getExitCode());
    }
}
