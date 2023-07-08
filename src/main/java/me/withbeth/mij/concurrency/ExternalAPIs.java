package me.withbeth.mij.concurrency;

import java.util.concurrent.FutureTask;
import java.util.function.IntConsumer;

public class ExternalAPIs {

    static int f(int x) {
        return x + 1;
    }

    static FutureTask<Integer> futureTaskF(int x) {
        return new FutureTask<>(() -> f(x));
    }

    static void callbackF(int x, IntConsumer dealWithResult) {
        // TODO : 비동기로 작업 완료시 콜백 호출하는 태스크 작성 후 반환
    }

    static int g(int x) {
        return x + 5;
    }

    static FutureTask<Integer> futureTaskG(int x) {
        return new FutureTask<>(() -> g(x));
    }

    static void callbackG(int x, IntConsumer dealWithResult) {
        // TODO : 비동기로 작업 완료시 콜백 호출하는 태스크 작성 후 반환
    }

}
