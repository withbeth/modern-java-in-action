package me.withbeth.mij.concurrency.async.completablefuture;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

class ShopClientTest {

    private static List<Shop> createShops(final int shopNumbers) {
        assert shopNumbers > 0;

        final PriceCalculator priceCalculator = new BlockingRandomPriceCalculator();
        final List<Shop> results = new ArrayList<>(shopNumbers);
        for (int i = 1; i <= shopNumbers; i++) {
            results.add(new MyShop("Shop #" + i, priceCalculator));
        }
        return results;
    }

    private void calculateExecutionTime(BiFunction<List<Shop>, String, List<String>> shopPricesFinder,
                                        List<Shop> shops) {
        final String productName = "PS5";
        final long start = System.nanoTime();

        System.out.println("Start to find shop numbers :" + shops.size());

        System.out.println(shopPricesFinder.apply(shops, productName));

        final long duration = ( System.nanoTime() - start ) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 16, 32})
    void findPriceParallely(final int shopNumbers) {
        calculateExecutionTime(ShopClient::findPricesParallely, createShops(shopNumbers));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 16, 32})
    void findPriceAsyncly(final int shopNumbers) {
        calculateExecutionTime(ShopClient::findPricesAsyncly, createShops(shopNumbers));
    }

}