package com.zyjclass.protection;

/**
 * 基于令牌桶算法的限流器
 * @author CAREYIJIAN$
 * @date 2024/1/29$
 */
public class TokenBuketRateLimiter {

    //思考：令牌桶用什么？list还是map   令牌用什么？String还是Object

    //代表令牌的数量，大于零说明有令牌可用放行，放行就减一，等于零就是没令牌了要阻拦
    private int tokens;

    //限流的本质就是令牌总数
    private final int capacity;

    //令牌桶的令牌没了就按照一定的速率给令牌桶加令牌，如每秒加500个，但是不能超过总数
    //可用用定时任务去加 --> 启动一个定时任务每秒执行一次
    //（对于单机版的限流器可以有更简单的操作，每有一个请求要发送的时候给他加一下就行
    private final int rate;

    //上一次放令牌的时间
    private Long lastTokenTime = System.currentTimeMillis();

    public TokenBuketRateLimiter(int capacity, int rate){
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = this.capacity;
    }

    /**
     * 判断请求是否可以放行
     * @return
     */
    public synchronized boolean allowRequest(){
        //1、给令牌桶添加令牌
        //计算从上一次到现在的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        Long timeInterval = currentTime - lastTokenTime;
         //如果间隔时间超过一秒,放令牌
        if (timeInterval >= 1000/rate){
            int needAddTokens = (int)(timeInterval * rate / 1000);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            this.lastTokenTime = System.currentTimeMillis();
        }

        //2、获取令牌,如果令牌桶中有令牌则放弃，否则拦截
        if (tokens > 0){
            tokens --;
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBuketRateLimiter tokenBuketRateLimiter = new TokenBuketRateLimiter(10,10);
        for (int i = 0; i < 1000; i++){
            try {
                Thread.sleep(10);
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
            boolean allowRequest = tokenBuketRateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }



    }

}
