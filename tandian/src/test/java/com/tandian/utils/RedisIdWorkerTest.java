package com.tandian.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisIdWorkerTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldGeneratePositiveId() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        RedisIdWorker worker = new RedisIdWorker(redisTemplate);

        long id = worker.nextId("order");

        assertTrue(id > 0);
    }

    @Test
    void shouldRejectSequenceOverflow() {
        when(valueOperations.increment(anyString())).thenReturn((1L << 32));
        RedisIdWorker worker = new RedisIdWorker(redisTemplate);

        assertThrows(IllegalStateException.class, () -> worker.nextId("order"));
    }
}
