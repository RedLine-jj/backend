package com.jj.redline.batch.scheduler;

import org.springframework.batch.core.Job;

public record ScheduledCrawlingJob(String siteName, Job job) {
}
