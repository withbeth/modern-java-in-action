package me.withbeth.mij.concurrency;

import java.util.concurrent.FutureTask;

public class ExternalAPIs {

    static int f(int x) {
        return x + 1;
    }

    static FutureTask<Integer> futureTaskF(int x) {
        return new FutureTask<>(() -> f(x));
    }

    static int g(int x) {
        return x + 5;
    }

    static FutureTask<Integer> futureTaskG(int x) {
        return new FutureTask<>(() -> g(x));
    }
}
