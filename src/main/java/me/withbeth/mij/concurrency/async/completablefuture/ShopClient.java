package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ShopClient {

    private static String formatShopPrice(Shop shop, Double productPrice) {
        return String.format("%s price is %.2f", shop.getName(), productPrice);
    }

    // 주어진 상점에, "순차적"으로 정보를 요청하여 주어진 상품명의 가격을 반환
    public static List<String> findPrices(List<Shop> shops, String productName) {
        return shops.stream()
                .map(shop -> formatShopPrice(shop, shop.getPrice(productName)))
                .collect(Collectors.toList());
    }

    // 주어진 상점에, "병렬 스트림으로" 요청을 병렬화
    public static List<String> findPricesParallely(List<Shop> shops, String productName) {
        return shops.parallelStream()
                .map(shop -> formatShopPrice(shop, shop.getPrice(productName)))
                .collect(Collectors.toList());
    }

    // non-blocking x async 하게 모든 상점의 가격 호출
    public static List<String> findPricesAsyncly(List<Shop> shops, String productName) {
        final List<CompletableFuture<String>> futurePrices = shops.stream()
                // 각 상점의 가격 요청을 non-blocking x async하게 호출
                .map(shop -> CompletableFuture.supplyAsync(() -> formatShopPrice(shop, shop.getPrice(productName))))
                .collect(Collectors.toList());

        // 스트림처리를 하나의 파이프라인으로 처리할경우, 스트림의 lazy 처리가 작동하여, 모든 가격 정보 요청이 동기적, 순차적으로 이뤄질수 있다.
        // 따라서, 별도 스트림으로 나누어 처리.

        return futurePrices.stream()
                // CF.join()을 이용해 비동기 연산이 끝나길 기다린다.
                // CF.join 연산 = Future.get과 같이 비동기 동작 결과 획득.
                // 차이점 = 계산중 예외발생하거나, 취소시 예외발생시, throw UNCHECKED exception
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

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
