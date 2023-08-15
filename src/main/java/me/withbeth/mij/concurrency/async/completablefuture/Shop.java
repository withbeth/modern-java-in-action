package me.withbeth.mij.concurrency.async.completablefuture;

import java.util.concurrent.Future;

public interface Shop {

    String getName();

    String getPrice(String productName);

    Future<String> getPriceAsync(String productName);

}
