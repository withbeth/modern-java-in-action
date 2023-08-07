
# CompletableFuture로 비동기 애플리케이션 만들기

# Q. 어떤걸 만드나요?
> 온라인 상점 중, 가장 저렴한 가격 제시하는 상점을 찾는 어플리케이션
 
# Q. 이걸로 어떤걸 배울 수 있죠?
- Open Async API제공방법
- SyncAPI를, Async적으로 소비하는 방법
- SyncAPI사용시, 코드를 non-blocking으로 만드는 방법.
- SyncAPI사용시, 여러 Async 동작을 파이프라인으로 연결, 동작 결과를 하나의 비동기 계산으로 합치는 방법.
  - > 예) 외부서비스로부터 할인코드 획득 -> 외부서비스로부터 할인율 획득 -> 원래 가격에 할인율 적용하여 최종결과 계산
- Async 완료 대응방법
  - > 예) 모든 상점에서 가격정보를 얻기까지 기다리는 것이 아니라, 각 가격 정보를 얻을때마다, "즉시" 최적가격 찾도록 대응

# Flow

## Interface정의
```j
public interface Shop {
    double getPrice(String productName);
}
```

## 가격을 찾는 동기메서드 구현
```
double getPrice(String productName);
```

## 가격 찾는 동기메서드를 비동기로 메서드로 변환
> 비동기 계산 결과를 표현할 수 있는 Future인터페이스로 반환값 표현
```
Future<Double> getPriceAsync(String productName);
```

## 비동기메서드 예외 처리 방법 설정

Client : Set Timeout.

API : 예외 발생시, CF의 completeExceptionally() 메서드에 예외정보를 설정.
- 이를 통해, Client에게 ExecutionException의 예외 파라미터로 예외 전달 가능.
```
Future<Double> getPriceAsync(String productName) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();

    new Thread(() ->{

        try {

            double price = calculatePrice(productName);
            // 계산 완료시, Future에 값 설정.
            // Note : complete() 메서드는 CAS연산을 이용해 값 설정.
            futurePrice.complete(price);

        } catch (Exception exception) {
            
            // 계산 도중 예외 발생시, 예외 정보를 클라이언트에게 전달
            futurePrice.completeExceptionally(exception);
            
        }

    }) .start();

    return futurePrice;
}
```

# QnA

## Q. How to test 비동기 메서드?

 