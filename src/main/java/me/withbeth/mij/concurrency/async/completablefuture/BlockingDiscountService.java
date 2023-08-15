package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.Random;

public class BlockingDiscountService implements DiscountService {
    private static final long ONE_SECOND = 1000L;
    private static final Random random = new Random();

    @Override
    public String applyDiscount(Quote quote) {
        return quote.getShopName()
                + " price is "
                + String.format("%.2f", calculatePrice(quote.getPrice(), quote.getDiscountCode()));
    }

    private static double calculatePrice(double price, DiscountCode code) {
        BlockingUtils.block(ONE_SECOND);
        return (price * (100 - code.getPercentage()) / 100);
    }
}
