package com.tandian.utils;

/**
 * Redis key 与过期时间统一管理。
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    /* ---------- 登录 ---------- */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final long LOGIN_CODE_TTL_MINUTES = 2L;
    public static final String LOGIN_CODE_LIMIT_KEY = "login:code:limit:";
    public static final long LOGIN_CODE_LIMIT_SECONDS = 60L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final long LOGIN_USER_TTL_MINUTES = 30L;

    /* ---------- 商铺缓存 ---------- */
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final long CACHE_SHOP_TTL_MINUTES = 30L;
    public static final long CACHE_NULL_TTL_MINUTES = 2L;
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop-type:";
    public static final long CACHE_SHOP_TYPE_TTL_MINUTES = 60L;

    /* ---------- 关注 ---------- */
    public static final String FOLLOW_KEY = "follow:";

    /* ---------- 分布式锁 ---------- */
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final long LOCK_SHOP_WAIT_SECONDS = 3L;
    public static final long LOCK_SHOP_LEASE_SECONDS = 10L;
    public static final String LOCK_ORDER_KEY = "lock:order:";

    /* ---------- 秒杀 ---------- */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String SECKILL_RATE_KEY = "seckill:rate:";

    /* ---------- 社区 / 签到 ---------- */
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String USER_SIGN_KEY = "sign:";

    /* ---------- 地理位置 ---------- */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /* ---------- 全局 ID ---------- */
    public static final String ID_WORKER_KEY = "icr:";
}
