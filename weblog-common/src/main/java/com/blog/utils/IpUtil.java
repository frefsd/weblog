package com.blog.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类，从 HttpServletRequest 中提取真实客户端 IP。
 *
 * <p><b>只信任 {@code X-Real-IP}</b>：该头由反向代理<b>覆盖写入</b>
 * （Nginx: {@code proxy_set_header X-Real-IP $remote_addr;}），客户端无法伪造。</p>
 *
 * <p>不再读取 {@code X-Forwarded-For} / {@code Proxy-Client-IP} 等由客户端请求头直接透传的字段，
 * 否则攻击者只需带一个随机 {@code X-Forwarded-For} 就能绕过所有基于 IP 的限流
 * （登录爆破、AI 接口刷额度、游戏接口）。</p>
 *
 * <p>部署要求：Nginx 必须配置 {@code proxy_set_header X-Real-IP $remote_addr;}，
 * 否则所有请求都会退化为代理地址，导致全站共享同一份限流配额。</p>
 */
public class IpUtil {

    private static final String UNKNOWN = "unknown";

    private IpUtil() {
    }

    /**
     * 获取客户端真实 IP。
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多级代理可能以逗号分隔，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? UNKNOWN : ip;
    }
}
