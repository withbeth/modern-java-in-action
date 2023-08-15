package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.Random;
import java.util.function.Supplier;

public class RandomDiscountCodeSupplier implements Supplier<Discount.Code> {
    private static final Random random = new Random();

    @Override
    public Discount.Code get() {
        final int codeNumbers = Discount.Code.values().length;
        return Discount.Code.values()[random.nextInt(codeNumbers)];
    }
}
