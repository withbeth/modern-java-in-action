package me.withbeth.mij.concurrency.async.completablefuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MyShopTest {

    @DisplayName("가격계산연산에 타임아웃 설정가능하여, 무한히 대기 하지 않아야 한다")
    @Test
    void canSetTimeOutForCalculatingPrice() {
        final long ONE_SECOND = 1000L;
        final long TWO_SECOND = 2000L;

        final Shop shop = new MyShop(
                "BestShop",
                ((productName) -> {
                    BlockingUtils.block(TWO_SECOND); return 1.0;
                }),
                ()-> Discount.Code.DIAMOND);

        Future<String> futurePrice = shop.getPriceAsync("favorite product");

        assertThatThrownBy(() -> futurePrice.get(ONE_SECOND, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    @DisplayName("가격계산 중 장애발생시, 어떠한 에러가 발생했는지 알 수 있어야 한다.")
    @Test
    void shouldKnowCalculatingPriceErrorCause() {
        final Shop shop = new MyShop(
                "BestShop",
                ((productName) -> {
                    throw new RuntimeException("알 수 없는 장애 발생");
                }),
                () -> Discount.Code.DIAMOND);

        Future<String> futurePrice = shop.getPriceAsync("favorite product");

        assertThatThrownBy(() -> futurePrice.get())
                .isInstanceOf(ExecutionException.class)
                .hasCause(new RuntimeException("알 수 없는 장애 발생"));
    }

}