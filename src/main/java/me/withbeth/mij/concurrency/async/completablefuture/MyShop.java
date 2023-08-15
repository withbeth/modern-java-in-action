package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class MyShop implements Shop {
    private final String shopName;
    private final PriceCalculator priceCalculator;
    private final Supplier<DiscountCode> couponSupplier;

    public MyShop(final String shopName,
                  final PriceCalculator priceCalculator,
                  final Supplier<DiscountCode> codeSupplier) {
        this.shopName = shopName;
        this.priceCalculator = priceCalculator;
        this.couponSupplier = codeSupplier;
    }

    private String formatShopPrice(Double productPrice, DiscountCode code) {
        return String.format("%s:%.2f:%s", getName(), productPrice, code);
    }

    @Override
    public String getName() {
        return shopName;
    }

    // blocking x sync
    @Override
    public String getPrice(String productName) {
        final double price = priceCalculator.calculatePrice(productName);
        final DiscountCode code = couponSupplier.get();
        return formatShopPrice(price, code);
    }

    @Override
    // non-blocking x async
    public Future<String> getPriceAsync(String productName) {
        // 예외발생시, 예외 정보 클라이언트에게 전달해주는 식으로 구현되어 있다.
        // Checkout AsyncSupply.run()
        return CompletableFuture.supplyAsync(() -> getPrice(productName));
    }

    // non-blocking x async
    public Future<String> getPriceAsyncOld(String productName) {
        CompletableFuture<String> futurePrice = new CompletableFuture<>();

        new Thread(() ->{

            try {

                String price = getPrice(productName);
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
