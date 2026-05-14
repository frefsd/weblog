package com.blog.utils;

/**
 * Redis 键前缀与过期时间常量集中管理。
 *
 * 格式约定：weblog:xxx.yyy.{dynamic}（冒号前为项目文件夹，冒号后使用点号分隔避免 GUI 多层嵌套）
 */
public class RedisConstants {

    /** Token 白名单前缀：weblog:login.token.{jwt} */
    public static final String LOGIN_TOKEN_KEY = "weblog:login.token.";

    /** Token 过期时间（秒），24 小时 */
    public static final Long LOGIN_TOKEN_TTL = 86400L;

    /** 前台文章详情缓存（TTL 30 分钟，由 RedisCacheManager 配置） */
    public static final String ARTICLE_DETAIL_CACHE = "weblog:article:detail";

    /** 前台首页文章列表缓存（TTL 5 分钟，由 RedisCacheManager 配置） */
    public static final String ARTICLE_INDEX_CACHE = "weblog:article:index";

    /** 前台分类列表缓存（TTL 30 分钟，由 RedisCacheManager 配置） */
    public static final String CATEGORY_LIST_CACHE = "weblog:category:list";

    /** 前台标签列表缓存（TTL 30 分钟，由 RedisCacheManager 配置） */
    public static final String TAG_LIST_CACHE = "weblog:tag:list";

    /** 博客详情缓存（TTL 30 分钟，由 RedisCacheManager 配置） */
    public static final String BLOG_SETTING_CACHE = "weblog:blog:setting";
}
