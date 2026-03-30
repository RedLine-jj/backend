package com.jj.redline.batch.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestockEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic restockTopic;

    public void publish(Long modelId) {
        log.info("[DbSnapshotWriter] 재입고 감지 -> Redis PUBLISH: modelId={}", modelId);
        redisTemplate.convertAndSend(restockTopic.getTopic(), String.valueOf(modelId));
    }
}
