package com.example.stockman.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository {

    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3000));
    }

    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    public String generateKey(Long key) {
        return key.toString();
    }
}
