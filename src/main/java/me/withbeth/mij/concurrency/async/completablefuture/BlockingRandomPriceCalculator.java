package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.Random;

public class BlockingRandomPriceCalculator implements PriceCalculator {
    private static final long ONE_SECOND = 1000L;
    private static final Random random = new Random();

    @Override
    public double calculatePrice(String productName) {
        block(ONE_SECOND);
        return random.nextDouble() * productName.charAt(0) + productName.charAt(1);
    }

    // 지연 흉내 헬퍼 메서드
    public static void block(long blockTime) {
        try {
            Thread.sleep(blockTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
