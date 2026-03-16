package com.jj.redline.api.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    private static final long TIMEOUT = 60 * 60 * 1000L;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("[SSE] 연결 종료: userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("[SSE] 타임아웃: userId={}", userId);
            emitter.complete();
        });
        emitter.onError(e -> {
            log.debug("[SSE] 에러: userId={}", userId);
            emitter.complete();
        });

        emitters.put(userId, emitter);
        log.info("[SSE] 연결: userId={}", userId);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitter.complete();
        }

        return emitter;
    }

    public void send(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.debug("[SSE] 전송 실패: userId={}", userId);
            emitter.complete();
        }
    }

    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }
}
