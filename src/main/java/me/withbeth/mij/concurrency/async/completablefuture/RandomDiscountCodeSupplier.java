package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.Random;
import java.util.function.Supplier;

public class RandomDiscountCodeSupplier implements Supplier<DiscountCode> {
    private static final Random random = new Random();

    @Override
    public DiscountCode get() {
        final int codeNumbers = DiscountCode.values().length;
        return DiscountCode.values()[random.nextInt(codeNumbers)];
    }
}
