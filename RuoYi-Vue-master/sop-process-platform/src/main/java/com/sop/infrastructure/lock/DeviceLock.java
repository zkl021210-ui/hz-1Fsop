package com.sop.infrastructure.lock;

/**
 * 设备维度分布式锁接口
 * <p>
 * 当前提供本地实现（LocalDeviceLock），后续可切换为 Redis 实现。
 *
 * @author SOP Team
 */
public interface DeviceLock {

    /**
     * 尝试获取锁，非阻塞，立即返回
     *
     * @param deviceSn 设备编码
     * @return true=获取成功, false=锁被占用
     */
    boolean tryLock(String deviceSn);

    /**
     * 释放锁
     *
     * @param deviceSn 设备编码
     */
    void unlock(String deviceSn);
}
