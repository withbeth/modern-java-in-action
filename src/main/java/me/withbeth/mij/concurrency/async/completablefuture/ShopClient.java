package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShopClient {

    public static void main(String[] args) {

        final Shop shop = new MyShop("BestShop", new BlockingRandomPriceCalculator());

        final long start = System.nanoTime();

        Future<Double> futurePrice = shop.getPriceAsync("favorite product");

        final long invocationTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Invocation returned after " + invocationTime + " ms");

        // non-blocking x async이므로, 다른 작업 수행
        doSomeThingElse();

        // 다른 작업 수행이 끝난후, 제품명에 대한 가격이 필요한 경우.
        try {
            // blocking until get the result
            final double price = futurePrice.get(2, TimeUnit.SECONDS);
            System.out.printf("Price is %.2f%n", price);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        final long retrievalTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Price returned after " + retrievalTime + " ms");
    }

    private static void doSomeThingElse() {
        System.out.println("Shop Client doing something else...");
    }
}
