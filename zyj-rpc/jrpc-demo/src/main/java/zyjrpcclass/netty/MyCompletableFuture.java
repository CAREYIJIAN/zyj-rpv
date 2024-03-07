package zyjrpcclass.netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
public class MyCompletableFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        /**
         * CompletableFuture可以获取子线程中的返回，过程中的结果，并可以在主线程中阻塞，等待其完成。
         */
        CompletableFuture<Integer> completableFuture = new CompletableFuture();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int i = 8;
            completableFuture.complete(i);
        }).start();

        // get方法是一个阻塞的方法
        Integer integer = completableFuture.get(1, TimeUnit.SECONDS);
        System.out.println(integer);
    }
}
