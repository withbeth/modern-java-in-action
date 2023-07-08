package me.withbeth.mij.concurrency;

public class UsingThreadManually {

    public static void main(String[] args) throws InterruptedException {

        int x = 1;
        PairResult.Builder resultBuilder = new PairResult.Builder();

        Thread t1 = new Thread(() -> resultBuilder.left(ExternalAPIs.f(x)));
        Thread t2 = new Thread(() -> resultBuilder.right(ExternalAPIs.g(x)));

        // start threads
        t1.start();
        t2.start();
        // waits threads for die
        t1.join();
        t2.join();

        PairResult result = resultBuilder.build();
        System.out.println("result = " + result);
    }

}
