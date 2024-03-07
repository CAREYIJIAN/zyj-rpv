package com.zyjclass.protection;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/29$
 */
public interface RateLimiter {
    /**
     * 是否允许新的请求进入
     * @return
     */
    boolean allowRequest();
}
