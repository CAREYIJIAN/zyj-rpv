package com.zyjclass.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易熔断器的实现
 * @author CAREYIJIAN$
 * @date 2024/1/29$
 */
//理论上：标准的断路器应该有三种状态  open  close   half_open，这里只选前两种
public class CircuitBreaker {

    //断路器状态是否打开
    private volatile  boolean isOpen = false;

    //搜集指标 异常的数量 比例
    //总的请求数
    private AtomicInteger requestCount = new AtomicInteger(0);

    //异常的请求数
    private AtomicInteger errorRequest = new AtomicInteger(0);

    //允许异常的阈值
    private int maxErrorRequest;
    //率
    private float maxErrorRate;

    public CircuitBreaker(int maxErrorRequest, float maxErrorRate){
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }

    //断路器的核心方法，判断是否开启
    public boolean isBreak(){
        //优先返回原则，如果已经打开了，就直接返回true
        if (isOpen){
            return true;
        }

        //需要判断数据指标，是否满足当前的阈值
        if (errorRequest.get() > maxErrorRequest){
            this.isOpen = true;
            return true;
        }

        if (errorRequest.get() > 0 && requestCount.get() >0
                && errorRequest.get()/(float)requestCount.get() > maxErrorRate)
        {
            this.isOpen = true;
            return true;
        }
        return false;
    }

    //重置熔断器,但实际上，当熔断器打开后，应该是隔一段时间将熔断器状态变为半打开状态，发送一次请求查看是否成功，成功在关闭熔断器。
    public void reset(){
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);
    }


    //每次发生请求、获取发生异常应该进行记录
    public void recordRequest(){
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest(){
        this.errorRequest.getAndIncrement();
    }


    public static void main(String[] args) {
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1F);

        new Thread(() ->{
            for (int i = 0; i < 1000; i++){
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                int num = new Random().nextInt(100);
                if (num > 70){
                    circuitBreaker.recordErrorRequest();
                }
                boolean isBreak = circuitBreaker.isBreak();
                String result = isBreak ? "断路器阻塞了请求" : "断路器放行了请求";
                System.out.println(result);
            }
        }).start();


        new Thread(() ->{
            for (;;){
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
                circuitBreaker.reset();
            }
        }).start();

        try {
            Thread.sleep(200000);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }

    }

}
