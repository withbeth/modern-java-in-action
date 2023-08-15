package me.withbeth.mij.concurrency.async.completablefuture;

public class Quote {
    private final String shopName;
    private final double price;
    private final DiscountCode discountCode;

    private Quote(String shopName, double price, DiscountCode discountCode) {
        this.shopName = shopName;
        this.price = price;
        this.discountCode = discountCode;
    }

    public static Quote parse(String s) {
        final String[] split = s.split(":");
        return new Quote(
                split[0],
                Double.parseDouble(split[1]),
                DiscountCode.valueOf(split[2])
        );
    }

    public String getShopName() {
        return shopName;
    }

    public double getPrice() {
        return price;
    }

    public DiscountCode getDiscountCode() {
        return discountCode;
    }
}
