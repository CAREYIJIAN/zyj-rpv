package com.zyjclass;

import com.zyjclass.annotation.TryTimes;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/16$
 */
public interface HelloJrpc {
    /**
     * 通用接口，server和client都需要依赖
     * @param msg 发送的具体消息
     * @return 返回的结果
     */
    @TryTimes(tryTimes = 3,intervalTime = 3000)
    String sayHi (String msg);
}
