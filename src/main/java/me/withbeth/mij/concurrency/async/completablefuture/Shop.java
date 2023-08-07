package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.concurrent.Future;

public interface Shop {

    String getName();

    double getPrice(String productName);

    Future<Double> getPriceAsync(String productName);

}
