package com.jj.redline.api.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRedisService {

    private static final String KEY_PREFIX = "notifications:unread:";
    private final StringRedisTemplate redisTemplate;

    public void increment(Long userId) {
        redisTemplate.opsForValue().increment(keyOf(userId));
    }

    public void decrement(Long userId) {
        String key = keyOf(userId);
        Long current = redisTemplate.opsForValue().decrement(key);
        if (current != null && current <= 0) {
            redisTemplate.delete(key);
        }
    }

    public long getUnreadCount(Long userId) {
        String val = redisTemplate.opsForValue().get(keyOf(userId));
        return val == null ? 0 : Long.parseLong(val);
    }

    public void reset(Long userId) {
        redisTemplate.delete(keyOf(userId));
    }

    private String keyOf(Long userId) {
        return KEY_PREFIX + userId;
    }
}
