package me.myh.lock;

import java.util.concurrent.*;

/**
 *
 * @author mayanhao
 * @date 2017/11/25
 */
public class Main {
    private static final int QTY = 10;

    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (int i = 0; QTY > i; i++) {
            service.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Thread " + Thread.currentThread().getId() + " is running");
                        latch.await();
                        DistributedLock lock = new DistributedLock("myhlock", "127.0.0.1:8989");
                        lock.lock();
                        Thread.sleep(5000);
                        lock.unlock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        latch.countDown();
    }
}
