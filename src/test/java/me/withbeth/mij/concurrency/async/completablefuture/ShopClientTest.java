package me.withbeth.mij.concurrency.async.completablefuture;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

class ShopClientTest {

    private static List<Shop> createShops(final int shopNumbers) {
        assert shopNumbers > 0;

        final PriceCalculator priceCalculator = new BlockingRandomPriceCalculator();
        final Supplier<Discount.Code> codeSupplier = new RandomDiscountCodeSupplier();

        final List<Shop> results = new ArrayList<>(shopNumbers);
        for (int i = 1; i <= shopNumbers; i++) {
            results.add(new MyShop("Shop #" + i, priceCalculator, codeSupplier));
        }
        return results;
    }

    private void calculateExecutionTime(Function<String, List<String>> shopPricesFinder) {

        System.out.println("Available CPU Core Numbers = " + Runtime.getRuntime().availableProcessors());

        final String productName = "PS5";

        final long start = System.nanoTime();

        System.out.println(shopPricesFinder.apply(productName));

        final long duration = ( System.nanoTime() - start ) / 1_000_000;

        System.out.println("Done in " + duration + " msecs");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 16, 32})
    void findPriceParallely(final int shopNumbers) {
        System.out.println("Shop numbers :" + shopNumbers);

        final ShopClient shopClient = new ShopClient(createShops(shopNumbers));
        calculateExecutionTime(shopClient::findPricesParallely);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 16, 32})
    void findPriceAsyncly(final int shopNumbers) {
        System.out.println("Shop numbers :" + shopNumbers);

        final ShopClient shopClient = new ShopClient(createShops(shopNumbers));
        calculateExecutionTime(shopClient::findPricesAsyncly);
    }

}