package me.withbeth.mij.concurrency.async.syncAndAsync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QuizExample {

    // 하나의 스레드를 이용해, work1() -> sleep(10s) -> work2()
    private static void optionOne() throws InterruptedException {
        work1();
        Thread.sleep(10000L);
        work2();
    }

    // 스레드풀 이용 : work1() -> 10초후 work2실행토록 스케줄링.
    private static void optionTwo() {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(1);
        // schedule first
        scheduledExecutorService.schedule(()-> work2(), 10L, TimeUnit.SECONDS);
        // work1
        //scheduledExecutorService.submit(()-> work1());
        work1();
        // shutdown
        scheduledExecutorService.shutdown();
    }

    private static void work1() {
        System.out.println("doing work1...");
    }

    private static void work2() {
        System.out.println("doing work2...");
    }

    public static void main(String[] args) throws InterruptedException {
        optionTwo();
    }

}
