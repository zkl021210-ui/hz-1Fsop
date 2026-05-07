package com.sop.infrastructure.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备锁本地实现（ConcurrentHashMap + ReentrantLock）
 * <p>
 * 后续可替换为 Redis 实现，接口不变。
 *
 * @author SOP Team
 */
@Slf4j
@Component
public class LocalDeviceLock implements DeviceLock {

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String deviceSn) {
        String key = "lock:sop:device:" + deviceSn;
        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
        boolean acquired = lock.tryLock();
        if (acquired) {
            log.debug("设备锁获取成功: deviceSn={}", deviceSn);
        } else {
            log.warn("设备锁获取失败(被占用): deviceSn={}", deviceSn);
        }
        return acquired;
    }

    @Override
    public void unlock(String deviceSn) {
        String key = "lock:sop:device:" + deviceSn;
        ReentrantLock lock = lockMap.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("设备锁释放: deviceSn={}", deviceSn);
        }
    }
}
