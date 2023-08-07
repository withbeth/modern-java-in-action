package me.withbeth.mij.concurrency.async.completablefuture;

public final class BlockingUtils {

    private BlockingUtils() {
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
