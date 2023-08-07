package me.withbeth.mij.concurrency.async.syncAndAsync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UsingExecutorService {
    public static void main(String[] args)
            throws ExecutionException, InterruptedException {
        int x = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Integer> y = executorService.submit(() -> ExternalAPIs.f(x));
        Future<Integer> z = executorService.submit(() -> ExternalAPIs.g(x));

        final int result = y.get() + z.get(); // blocking
        System.out.println("result = " + result);

        executorService.shutdown();


    }
}
