package com.blog.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简易滑动窗口限流器，基于 IP 维度。
 * <p>
 * 线程安全：所有操作通过单一 {@link ReentrantLock} 串行化，
 * 保证"检查-追加"的原子性（避免并发下多个线程同时通过检查而突破 maxCount）。
 * <p>
 * 内存控制：每次调用会清除窗口内过期的请求记录；当 key 数量超过阈值时
 * 触发一次全量过期清理，防止伪造 IP 造成内存膨胀。
 */
@Component
public class RateLimiter {

    /** store 中 key 数量达到该值后触发全量过期清理 */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Deque<Long>> store = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 检查是否允许通过，线程安全。
     *
     * @param key      限流维度（如 IP）
     * @param maxCount 时间窗口内最大请求数
     * @param windowMs 时间窗口（毫秒）
     * @return true 允许通过，false 被限流
     */
    public boolean tryAcquire(String key, int maxCount, long windowMs) {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMs;

            Deque<Long> deque = store.get(key);
            if (deque == null) {
                deque = new ArrayDeque<>();
                store.put(key, deque);
            }

            // 清除窗口外的过期请求记录
            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }

            if (deque.size() >= maxCount) {
                return false;
            }

            deque.addLast(now);

            // 惰性全量清理：防止 key 无限膨胀（内存 DoS 防护）
            if (store.size() > CLEANUP_THRESHOLD) {
                store.entrySet().removeIf(e -> {
                    Deque<Long> d = e.getValue();
                    return d.isEmpty() || d.peekLast() < cutoff;
                });
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** 获取当前窗口内的请求数 */
    public int getCount(String key, long windowMs) {
        lock.lock();
        try {
            Deque<Long> deque = store.get(key);
            if (deque == null) {
                return 0;
            }
            long cutoff = System.currentTimeMillis() - windowMs;
            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }
            return deque.size();
        } finally {
            lock.unlock();
        }
    }

    /** 清理某个 key */
    public void reset(String key) {
        lock.lock();
        try {
            store.remove(key);
        } finally {
            lock.unlock();
        }
    }
}
