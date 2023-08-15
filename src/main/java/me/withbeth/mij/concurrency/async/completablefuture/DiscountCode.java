package me.withbeth.mij.concurrency.async.completablefuture;

public enum DiscountCode {
    NONE(0),
    SILVER(5),
    GOLD(10),
    PLATINUM(15),
    DIAMOND(20),
    ;

    private final int percentage;

    DiscountCode(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() {
        return percentage;
    }

}
