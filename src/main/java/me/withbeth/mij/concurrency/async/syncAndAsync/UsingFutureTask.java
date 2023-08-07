package me.withbeth.mij.concurrency.async.syncAndAsync;

import java.util.concurrent.*;

public class UsingFutureTask {

    public static void main(String[] args)
            throws ExecutionException, InterruptedException {
        int x = 1;
        FutureTask<Integer> y = ExternalAPIs.futureTaskF(x);
        FutureTask<Integer> z = ExternalAPIs.futureTaskG(x);

        y.run();
        z.run();

        try {
            final int result = y.get(1L, TimeUnit.SECONDS)
                    + z.get(1L, TimeUnit.SECONDS);
            System.out.println("result = " + result);;
        } catch (TimeoutException timeoutException) {
            System.out.printf(timeoutException.getMessage());
        }
    }
}
