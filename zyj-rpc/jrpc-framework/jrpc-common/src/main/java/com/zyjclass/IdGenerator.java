package com.zyjclass;

import com.zyjclass.utils.DateUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * 请求id的生成器
 * @author CAREYIJIAN$
 * @date 2024/1/22$
 */
public class IdGenerator {
    //LongAdder是单机版本的线程安全的id发号器，一旦变成集群状态，就不管事了
    /*private static LongAdder longAdder = new LongAdder();
    public static long getId(){
        longAdder.increment();
        return longAdder.sum();
    }*/
    /**雪花算法 -- 世界上没有一片雪花是一样的
       id:
          机房号 5bit 32个
          机器号 5bit 32个
          时间戳 （原本64位表示的时间，必须减少到42位，自由选择一个比较近的时间）
          序列号 12bit(由于同一个机房的同一个机器号的同一个时间可能因为并发量很大需要多个id，所以再加上一个序列号)
       long是64bit
       规划如何使用这个64位生成唯一id：（算好各部分，通过按位左移再求或，最终合成64位的id）
       00000000|00000000|00000000|00000000|00000000|00000000|00000000|00000000|
       时间戳（42位） --> 机房号（5位） --> 机器号（5） --> 序列号（12）
     */
    //起始时间戳
    public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();
    //机房号占位数
    public static final long DATA_CENTER_BIT = 5L;
    //机器号占位数
    public static final long MACHINE_BIT = 5L;
    //序列号占位数
    public static final long SEQUENCE_BIT = 12L;
    //根据位数算，可取的最大值 性能低的做法：(long) Math.pow(2,5) - 1 (减一的原因是从零开始)
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    //各部分左移的位数
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    //各部分的值
    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    //时钟回拨的问题，需要处理
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId){
        //判断传入的参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId(){
        //第一步：处理时间戳的问题
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;

        //判断时钟回拨
        if (timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");
        }

        //sequenceId需要做一些处理，如果是同一个时间节点，必须自增
        if (timeStamp == lastTimeStamp){
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }

        //执行结束后将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();

        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        //获取当前时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        //判断当前时间戳和上次是否一样
        while (current == lastTimeStamp){
            //一样就再拿
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        for (int i = 0; i < 1000; i++){
            new Thread(() -> {
                System.out.println(idGenerator.getId());
            }).start();
        }
    }


}
