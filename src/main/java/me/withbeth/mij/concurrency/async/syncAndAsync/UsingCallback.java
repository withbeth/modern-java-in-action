package me.withbeth.mij.concurrency.async.syncAndAsync;

public class UsingCallback {

    public static void main(String[] args) {
        int x = 1;

        PairResult.Builder resultBuilder = new PairResult.Builder();

        ExternalAPIs.callbackF(x, value -> {
            resultBuilder.left(value);
            System.out.println(resultBuilder);
        });

        ExternalAPIs.callbackG(x, value -> {
            resultBuilder.right(value);
            System.out.println(resultBuilder);
        });

    }
}
