package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MyShop implements Shop {
    private final String shopName;
    private final PriceCalculator priceCalculator;

    public MyShop(final String shopName,
                  final PriceCalculator priceCalculator) {
        this.shopName = shopName;
        this.priceCalculator = priceCalculator;
    }

    @Override
    public String getName() {
        return shopName;
    }

    // blocking x sync
    @Override
    public double getPrice(String productName) {
        return priceCalculator.calculatePrice(productName);
    }

    @Override
    // non-blocking x async
    public Future<Double> getPriceAsync(String productName) {
        // 예외발생시, 예외 정보 클라이언트에게 전달해주는 식으로 구현되어 있다.
        // Checkout AsyncSupply.run()
        return CompletableFuture.supplyAsync(() -> priceCalculator.calculatePrice(productName));
    }

    // non-blocking x async
    public Future<Double> getPriceAsyncOld(String productName) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();

        new Thread(() ->{

            try {

                double price = priceCalculator.calculatePrice(productName);
                // 계산 완료시, Future에 값 설정.
                // Note : complete() 메서드는 CAS연산을 이용해 값 설정.
                futurePrice.complete(price);

            } catch (Exception exception) {

                // 계산 도중 예외 발생시, 예외 정보를 클라이언트에게 전달
                futurePrice.completeExceptionally(exception);

            }

        }).start();

        return futurePrice;
    }
}
