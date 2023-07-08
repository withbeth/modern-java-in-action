package me.withbeth.mij.concurrency;

public class UsingThreadManually {

    public static void main(String[] args) throws InterruptedException {

        int x = 1;
        PairResult.Builder resultBuilder = new PairResult.Builder();

        Thread t1 = new Thread(() -> resultBuilder.left(f(x)));
        Thread t2 = new Thread(() -> resultBuilder.right(g(x)));

        // start threads
        t1.start();
        t2.start();
        // waits threads for die
        t1.join();
        t2.join();

        PairResult result = resultBuilder.build();
        System.out.println("result = " + result);
    }

    static int f(int x) {
        return x + 1;
    }

    static int g(int x) {
        return x + 5;
    }

    record PairResult(int left, int right) {
        static class Builder {
            private int left;
            private int right;
            Builder left(int val) {
                left = val;
                return this;
            }
            Builder right(int val) {
                right = val;
                return this;
            }
            PairResult build() {
                return new PairResult(left, right);
            }
        }

        @Override
        public String toString() {
            return "{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

}
