package me.withbeth.mij.concurrency.async.syncAndAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsingCompletableFuture {
    public static void main(String[] args)
            throws ExecutionException, InterruptedException {
        int x = 1;
        ExecutorService executorService =
                Executors.newFixedThreadPool(3);
        CompletableFuture<Integer> a = new CompletableFuture<>();
        CompletableFuture<Integer> b = new CompletableFuture<>();
        // 두 작업을 합치는(조합하는) 별도 태스크 이용
        // - Future a, b의 결과를 알지 못하는 상태에서, 두 연산이 끝났을때 실행할 태스크 작성.
        CompletableFuture<Integer> c = a.thenCombine(b, (y, z) -> y + z);
        // Q. thenCombineAsync()?

        executorService.submit(() -> a.complete(ExternalAPIs.f(x)));
        executorService.submit(() -> b.complete(ExternalAPIs.g(x)));

        // 다른 두 작업이 끝날때까지 실행 X (=먼저 시작해서 blocking하지 않는다)
        Integer result = c.get();
        System.out.println("result = " + result);

        executorService.shutdown();
    }
}
