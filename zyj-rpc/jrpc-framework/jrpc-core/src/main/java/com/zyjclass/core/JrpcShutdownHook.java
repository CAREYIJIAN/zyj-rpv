package com.zyjclass.core;

import java.util.concurrent.TimeUnit;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/31$
 */
public class JrpcShutdownHook extends Thread {

    @Override
    public void run() {
        //1.打开挡板 (boolean 需要线程安全)
        ShutDownHolder.BAFFLE.set(true);

        //2.等待计数器归零（意味着正常的请求处理结束）
        long start = System.currentTimeMillis();
        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShutDownHolder.REQUEST_COUNTER.sum() == 0L || System.currentTimeMillis() - start > 10000){
                break;
            }
        }


        //3.阻塞结束后放行。执行其他操作（释放资源）



        super.run();
    }
}
