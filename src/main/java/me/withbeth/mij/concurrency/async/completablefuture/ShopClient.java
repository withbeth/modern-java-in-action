package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ShopClient {

    private final List<Shop> shops;
    private final DiscountService discountService;

    // custom thread pool for CF operations
    private final Executor executor;

    public ShopClient(final List<Shop> shops,
                      final DiscountService discountService) {
        this.shops = shops;
        this.executor = Executors.newFixedThreadPool(
                // shops.size 또는, 최대 100개 까지의 스레드만 허용
                Math.min(shops.size(), 100),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        // 메인 종료전, 스레드 종료가능토록 데몬스레드화.
                        // 이 예제에선, 이를 통한 데이터 일관성 걱정 X
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
        this.discountService = discountService;
    }

    // 주어진 상점에, "순차적"으로 정보를 요청하여 주어진 상품명의 가격을 반환
    public List<String> findPrices(String productName) {
        return this.shops.stream()
                .map(shop -> shop.getPrice(productName))
                .map(Quote::parse)
                .map(discountService::applyDiscount)
                .collect(Collectors.toList());
    }

    // 주어진 상점에, "병렬 스트림으로" 요청을 병렬화
    public List<String> findPricesParallely(String productName) {
        return this.shops.parallelStream()
                .map(shop -> shop.getPrice(productName))
                .map(Quote::parse)
                .map(discountService::applyDiscount)
                .collect(Collectors.toList());
    }

    // non-blocking x async 하게 모든 상점의 가격 호출
    public List<String> findPricesAsyncly(String productName) {
        final List<CompletableFuture<String>> futurePrices = this.shops.stream()
                // 동기작업인 상점 가격 조회를 비동적으로 수행.
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice(productName), executor))
                // Quote객체로 Parsing(NO IO waiting needed)
                // Note : CF.thenApply()는, CF가 끝날때까지 블록하지 않는다.
                //        즉, CF가 동작을 완전히 완료후에, thenApply()로 전달된 람다표현식을 적용한다.
                .map(completableFuture -> completableFuture.thenApply(Quote::parse))
                // 2개의 비동기 연산을 파이프라이닝 using CF.thenCompose()
                .map(completableFuture -> completableFuture.thenCompose(quote ->
                        // 동기작업인 할인률 획득을 비동기적으로 수행
                        // 2번째 비동기 연산은, 첫번째 비동기 연산 결과를 입력으로 사용.
                        CompletableFuture.supplyAsync(() -> discountService.applyDiscount(quote), executor)))
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

        final Shop shop = new MyShop(
                "BestShop",
                new BlockingRandomPriceCalculator(),
                new RandomDiscountCodeSupplier());

        final long start = System.nanoTime();

        Future<String> futurePrice = shop.getPriceAsync("favorite product");

        final long invocationTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Invocation returned after " + invocationTime + " ms");

        // non-blocking x async이므로, 다른 작업 수행
        doSomeThingElse();

        // 다른 작업 수행이 끝난후, 제품명에 대한 가격이 필요한 경우.
        try {
            // blocking until get the result
            final String price = futurePrice.get(2, TimeUnit.SECONDS);
            System.out.println("price = " + price);

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
