package com.ljl.studyexammanagementsystem.lock;

import com.ljl.studyexammanagementsystem.config.AppProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {

    private final ObjectProvider<RedissonClient> redissonClientProvider;
    private final AppProperties properties;

    public DistributedLockService(ObjectProvider<RedissonClient> redissonClientProvider,
                                  AppProperties properties) {
        this.redissonClientProvider = redissonClientProvider;
        this.properties = properties;
    }

    public <T> T execute(String lockKey, Callable<T> action) {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null || !properties.getCache().isEnabled()) {
            return call(action);
        }
        RLock lock = client.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(properties.getCache().getLockWaitMillis(), TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new IllegalStateException("操作正在处理中，请勿重复提交");
            }
            return action.call();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取操作锁被中断", ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("操作执行失败", ex);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private <T> T call(Callable<T> action) {
        try {
            return action.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("操作执行失败", ex);
        }
    }
}