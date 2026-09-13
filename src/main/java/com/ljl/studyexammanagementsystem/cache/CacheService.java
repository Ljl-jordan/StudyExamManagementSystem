package com.ljl.studyexammanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ljl.studyexammanagementsystem.config.AppProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

@Service
public class CacheService {

    private static final String NULL_VALUE = "__NULL__";

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder failures = new LongAdder();
    private final LongAdder evictions = new LongAdder();

    public CacheService(ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                        ObjectMapper objectMapper,
                        AppProperties properties) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.getCache().isEnabled() && redisTemplateProvider.getIfAvailable() != null;
    }

    public <T> T get(String key, Class<T> type) {
        String json = getJson(key);
        if (json == null || NULL_VALUE.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            failures.increment();
            evict(key);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> type) {
        String json = getJson(key);
        if (json == null || NULL_VALUE.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            failures.increment();
            evict(key);
            return null;
        }
    }

    public <T> T getOrLoad(String key, Class<T> type, long ttlSeconds, Supplier<T> loader) {
        if (!isEnabled()) {
            return loader.get();
        }
        String cacheKey = normalizeKey(key);
        String json = getRaw(cacheKey);
        if (NULL_VALUE.equals(json)) {
            hits.increment();
            return null;
        }
        if (json != null) {
            try {
                hits.increment();
                return objectMapper.readValue(json, type);
            } catch (Exception ex) {
                failures.increment();
                evict(cacheKey);
            }
        }
        misses.increment();
        String mutexKey = cacheKey + ":mutex";
        Boolean locked = setIfAbsent(mutexKey, "1", properties.getCache().getLockWaitMillis(), TimeUnit.MILLISECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                json = getRaw(cacheKey);
                if (json != null) {
                    return NULL_VALUE.equals(json) ? null : objectMapper.readValue(json, type);
                }
                T value = loader.get();
                put(cacheKey, value, ttlSeconds);
                return value;
            } catch (Exception ex) {
                failures.increment();
                return loader.get();
            } finally {
                deleteQuietly(mutexKey);
            }
        }
        return loader.get();
    }

    public void put(String key, Object value) {
        put(key, value, properties.getCache().getDefaultTtlSeconds());
    }

    public void put(String key, Object value, long ttlSeconds) {
        if (!isEnabled()) {
            return;
        }
        try {
            String cacheKey = normalizeKey(key);
            String json = value == null ? NULL_VALUE : objectMapper.writeValueAsString(value);
            long actualTtl = value == null ? properties.getCache().getNullTtlSeconds() : randomize(ttlSeconds);
            requireTemplate().opsForValue().set(cacheKey, json, actualTtl, TimeUnit.SECONDS);
            if (properties.getCache().isLocalCache()) {
                localCache.put(cacheKey, json);
            }
        } catch (Exception ex) {
            failures.increment();
        }
    }

    public void evict(String key) {
        if (!isEnabled() || !StringUtils.hasText(key)) {
            return;
        }
        deleteQuietly(normalizeKey(key));
        evictions.increment();
    }

    public void evictAll(Collection<String> keys) {
        if (keys != null) {
            for (String key : keys) {
                evict(key);
            }
        }
    }

    public void evictByPrefix(String prefix) {
        if (!isEnabled() || !StringUtils.hasText(prefix)) {
            return;
        }
        String pattern = normalizeKey(prefix) + "*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
        try {
            Set<String> keys = requireTemplate().execute((RedisCallback<Set<String>>) connection -> {
                Set<String> result = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(options);
                try {
                    while (cursor.hasNext()) {
                        result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                } finally {
                    try {
                        cursor.close();
                    } catch (IOException ignored) {
                        // cursor close failure is non-fatal
                    }
                }
                return result;
            });
            if (keys != null) {
                for (String cacheKey : keys) {
                    deleteQuietly(cacheKey);
                    localCache.invalidate(cacheKey);
                    evictions.increment();
                }
            }
        } catch (Exception ex) {
            failures.increment();
        }
    }

    public long increment(String key, long ttlSeconds) {
        if (!isEnabled()) {
            return 1L;
        }
        String cacheKey = normalizeKey(key);
        Long value = requireTemplate().opsForValue().increment(cacheKey, 1L);
        if (value != null && value == 1L) {
            requireTemplate().expire(cacheKey, ttlSeconds, TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }

    public CacheStats stats() {
        return new CacheStats(hits.sum(), misses.sum(), failures.sum(), evictions.sum(), isEnabled());
    }

    private String getJson(String key) {
        if (!isEnabled()) {
            return null;
        }
        return getRaw(normalizeKey(key));
    }

    private String getRaw(String cacheKey) {
        try {
            String value = requireTemplate().opsForValue().get(cacheKey);
            if (value == null && properties.getCache().isLocalCache()) {
                value = localCache.getIfPresent(cacheKey);
            }
            return value;
        } catch (Exception ex) {
            failures.increment();
            return null;
        }
    }

    private Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        try {
            return requireTemplate().opsForValue().setIfAbsent(key, value, timeout, unit);
        } catch (Exception ex) {
            failures.increment();
            return false;
        }
    }

    private void deleteQuietly(String key) {
        try {
            requireTemplate().delete(key);
            localCache.invalidate(key);
        } catch (Exception ex) {
            failures.increment();
        }
    }

    private StringRedisTemplate requireTemplate() {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template == null) {
            throw new IllegalStateException("RedisTemplate is not available");
        }
        return template;
    }

    private String normalizeKey(String key) {
        String prefix = properties.getCache().getKeyPrefix();
        if (!StringUtils.hasText(prefix) || key.startsWith(prefix + ":")) {
            return key;
        }
        return prefix + ":" + key;
    }

    private long randomize(long ttlSeconds) {
        long base = Math.max(1L, ttlSeconds);
        long offset = Math.max(1L, base / 10L);
        return base + ThreadLocalRandom.current().nextLong(offset + 1L);
    }

    public static class CacheStats {
        private final long hits;
        private final long misses;
        private final long failures;
        private final long evictions;
        private final boolean enabled;

        public CacheStats(long hits, long misses, long failures, long evictions, boolean enabled) {
            this.hits = hits;
            this.misses = misses;
            this.failures = failures;
            this.evictions = evictions;
            this.enabled = enabled;
        }

        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getFailures() { return failures; }
        public long getEvictions() { return evictions; }
        public boolean isEnabled() { return enabled; }
    }
}
