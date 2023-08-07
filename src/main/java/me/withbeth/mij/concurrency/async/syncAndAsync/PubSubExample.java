package me.withbeth.mij.concurrency.async.syncAndAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * 예제 : 두개의 정보 소스로부터 발생하는 이벤트를 합쳐, 발행하는 예
 * 예: 엑셀 시트의 C3 = C1 + C2
 * C1 or C2 값이 갱신되면, C3에도 새로운 값 반영
 */
public class PubSubExample {

    private interface SimplePublisher<T> {
        void subscribe(SimpleSubscriber<? super T> subscriber);
    }

    private interface SimpleSubscriber<T> {
        void onNext(T t);
    }

    private static class SimpleCell implements
            SimplePublisher<Integer>,
            SimpleSubscriber<Integer> {

        private final String name;
        private final List<SimpleSubscriber<? super Integer>> subscribers = new ArrayList<>();
        private int value = 0;

        public SimpleCell(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void subscribe(SimpleSubscriber<? super Integer> subscriber) {
            this.subscribers.add(subscriber);
        }

        private void publish() {
            subscribers.forEach(subscriber -> subscriber.onNext(this.value));
        }

        @Override
        public void onNext(Integer changedValue) {
            this.value = changedValue;
            System.out.println("[Changed Value]" + this.name + ":" + this.value);
            publish();
        }
    }

    private static class PairCell extends SimpleCell {
        private int leftValue = 0;
        private int rightValue = 0;

        public PairCell(String name) {
            super(name, 0);
        }

        public void setLeftValue(int leftValue) {
            this.leftValue = leftValue;
            onNext(this.leftValue + this.rightValue);
        }

        public void setRightValue(int rightValue) {
            this.rightValue = rightValue;
            onNext(this.leftValue + this.rightValue);
        }
    }

    public static void main(String[] args) {
        SimpleCell c1 = new SimpleCell("C1", 1);
        SimpleCell c2 = new SimpleCell("C2", 5);
        // Q. c1 or c2 값이 바뀌었을때, c3이 두 값을 더하도록 하고 싶다.
        // A. c1 or c2의 이벤트 발생시, c3이 구독.
        // A. 또한, Cell은, Publisher이자 Subscriber
        //SimpleCell c3 = new SimpleCell("C3", 0);
        PairCell c3 = new PairCell("C3");

        // c3이, c1을 구독해야 되는거 아닌가?
        // or c1.addSubscriber(c3);
        c1.subscribe(c3::setLeftValue); // ?! Method Signatur가 같기에 이렇게 넘길수 있나보다.
        c2.subscribe(c3::setRightValue);

        c1.onNext(10);
        c2.onNext(50);

        // Q. 아니, 두 값을 더해야 되는데?
        // A. left, right result 갖는 객체 이용

    }
}
