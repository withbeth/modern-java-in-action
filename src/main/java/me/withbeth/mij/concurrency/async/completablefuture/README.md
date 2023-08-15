# CompletableFuture로 비동기 애플리케이션 만들기

# Q. 어떤걸 만드나요?

> 온라인 상점 중, 가장 저렴한 가격 제시하는 상점을 찾는 어플리케이션

# Q. 이걸로 어떤걸 배울 수 있죠?

- 비동기API제공 방법
  - > A. 반환값을 Future인터페이스로. (비동기 계산 결과 표현하는 객체 반환)
- 동기서비스를 비동기적으로 소비하는 방법
  - > A. CompletableFuture 내부 연산으로 만들어, 별도 스레드를 할당해 연산 수행토록 변경.
- 여러 비동기 연산의 파이프라인화하여, 연산 결과를 하나로 합치는 방법
  - > 예) 외부서비스로부터 할인코드 획득 -> 외부서비스로부터 할인율 획득 -> 원래 가격에 할인율 적용하여 최종결과 계산
- 비동기 연산 완료 대응방법
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

# 비동기API제공 방법

## API 개선 : 비동기 메서드 구현 

> 비동기 계산 결과를 표현할 수 있는 Future인터페이스로 반환값 표현

```
Future<Double> getPriceAsync(String productName);
```

### 비동기메서드 예외 처리 방법 설정

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

    }).start();

    return futurePrice;
}
```

### CompletableFuture의 팩토리 메서드를 이용한 구현 개선

```
@Override
// non-blocking x async
public Future<Double> getPriceAsync(String productName) {
    // 예외발생시, 예외 정보 클라이언트에게 전달해주는 식으로 구현되어 있다.
    // Checkout AsyncSupply.run()
    return CompletableFuture.supplyAsync(() -> priceCalculator.calculatePrice(productName));
}
```

- CF.supplyAsync()를 이용해 Supplier를 넘기는 형식으로 구현 간단화.
- 이때 해당 작업을 실행하는 Executor는 다음과 같다.
    - **_Default : CF는 `ForkJoinPool`의 Executor중 하나를 선택해, Supplier를 실행하여 비동기적으로 결과 생성._**
    - **_Customize : suuplyAsync()의 두번째 인자로, `실행할 Executor 지정 가능`_**

-----

# 동기서비스를 비동기적으로 소비하는 방법

## Shop API가 동기API만 지원할 경우, 최저 가격 검색 어플리케이션 구현

이번에는 반대로, 클라이언트 입장에서, Shop API가 Blocking x Sync API만 제공한다고 가정.

즉, 블록동기메서드를 이용할 수 밖에 없는 상황에서, 다음의 요구사항을 진행.

- 비동기적으로 여러 상점에 질의하며 블록되는 상황을 피해 최저 가격 검색

가장 간단히 구현 가능한, "순차적"으로 ShopAPI를 호출해 가격을 획득하는 방법부터 구현하여 개선시켜 보자.

## 모든 상점에 "순차적으로" 가격을 요청하는 메서드 작성

```
// 주어진 상점에, "순차적"으로 정보를 요청하여 주어진 상품명의 가격을 반환
public List<Double> findPrices(List<Shop> shops, String productName) {
    return shops.stream()
            .map(shop -> shop.getPrice(productName))
            .collect(Collectors.toList());
}
```

- 대략 `4초` 소요 (각 Shop의 가격 요청 반환까지 1초동안 Blocking걸린다고 가정하고, 4가지 Shop이 주어진 경우)

## 개선 : 병렬스트림으로 요청 병렬화하기

```
// 주어진 상점에, "병렬 스트림으로" 요청을 병렬화
public List<Double> findPricesParallely(List<Shop> shops, String productName) {
    return shops.parallelStream()
            .map(shop -> shop.getPrice(productName))
            .collect(Collectors.toList());
}
```

- 대략 `1초` 소요 (각 Shop의 가격 요청 반환까지 1초동안 Blocking걸린다고 가정하고, 4가지 Shop이 주어진 경우)

## 개선 : `findPrices()`의 비동기화 using CF

```
// non-blocking x async 하게 모든 상점의 가격 호출
public List<Double> findPricesAsyncly(List<Shop> shops, String productName) {
    final List<CompletableFuture<Double>> futurePrices = shops.stream()
            // 각 상점의 가격 요청을 non-blocking x async하게 호출
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice(productName)))
            .collect(Collectors.toList());

    // 스트림처리를 하나의 파이프라인으로 처리할경우, 스트림의 lazy 처리가 작동하여, 모든 가격 정보 요청이 동기적, 순차적으로 이뤄질수 있다.
    // 따라서, 별도 스트림으로 나누어 처리.

    return futurePrices.stream()
            // CF.join()을 이용해 비동기 연산이 끝나길 기다린다.
            // CF.join 연산 = Future.get과 같이 비동기 동작 결과 획득. 
            // 차이점 = 계산중 예외발생하거나, 취소시 예외발생시, throw UNCHECKED exception 
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
}
```

- 대략 `1초` 소요 (각 Shop의 가격 요청 반환까지 1초동안 Blocking걸린다고 가정하고, 4가지 Shop이 주어진 경우)

## 성능측정 : 병렬스트림 vs 비동기 호출

> Macbook air M2 기준 (Core CPU수 = 8개)

| ShopSize | 병렬스트림  | CF (ForkJoinPool) |
|----------|--------|---------------------|
| 1        | 1006ms | 1009ms              |
| 4        | 1007ms | 1006ms              |
| 8        | 1003ms | 2009ms              |
| 16       | 2007ms | 3012ms              |
| 32       | 4017ms | 5018ms              |

[ 병렬 스트림 ]
- CPU Core수 = 8개.
- 따라서, 8개까지는 각 상점을 CPU 코어에 할당하여, 1초내 완료.
- 그 이상은, 누군가의 코어가 작업 완료하여 코어가 빌 때까지 대기.

[ CF (ForkJoinPool) ]
- 기본 스레드 풀 개수 = 4개. 
- 따라서, 4개 상점까지는 1초내 완료.
- 그 이상은, 누군가의 스레드가 작업완료하여 스레드가 빌때 까지 대기.

## So What ?

> 그렇다면, `병렬스트림`과, `CF이용한 비동기 호출 방법`중,어느게 더 좋은 방법일까?

- Answer : CF는 병렬 스트림에 비해, **_다양한 Executor(ThreadPool) 지정 + 해당 프로그램에 최적화된 설정 및 정책 적용_** 가능.

## Java 스레드풀 주의 할 점
> JavaAPI의 스레드풀 관리방법은, DB POOL의 그것 과 다르다.

JavaAPI는 `corePool, maxPool, queueCap` 존재시,
`corePool + queueCap`이 설정개수를 넘을 때에만, maxPool까지 채운다.

## 그렇다면, 위 CF연산에는, 몇개의 스레드를 이용해야 최적화 가능한가?

### 스레드 수 최적값 찾는 방법.

`최적 스레드 수 = CPU코어수 * CPU활용비율(0~1) * (1 + WAITING/COMPUTING 비율(0~100))`

- CPU코어수 = `Runtime.getRuntime().availableProcessors()`로 알 수 있다.
- CPU활용비율 = 0~1 사이 값을 갖는 CPU 활용 비율
- WAITING/COMPUTING 비율 = 대기시간과, 계산시간 비율

### 그럼, 이 예제에서의 최적의 스레드 수는 몇개 인가?

위 공식에 값을 대입해 보자.

- CPU코어수 = 8
- CPU활용비율(0~1) = 1 (100%)라 가정.
- WAITING/COMPUTING비율(0~100) = 상점의 응답 기다리는게 99%이니, 100이라 가정.

따라서, 최적 스레드수 = 8 * 1 * 101 = 808 개가 된다.

?!

하지만, 상점 수 보다 많은 스레드를 가지고 있어봐야 사용할 가능성이 없기에, 그 보다 많은 스레드는 불필요하다.

오히려, 스레드 수가 많으면 서버가 크래시할 가능성이 있다. (OS에서 생성가능한 스레드 개수는 정해져 있으므로)

결론적으로, **한 상점당 한 스레드 (1 Thread per 1 Shop)** 할당 가능하도록 Executor를 설정하면 좋을 것 같다.

그래야, 스레드 대기 없이 모든 스레드들이 non-blocking x async 연산이 가능하고, 후에 merge가 가능할 것.

```
this.executor = Executors.newFixedThreadPool(
        Math.min(shops.size(), 100), // shops.size 또는, 최대 100개 까지의 스레드만 허용
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true); // 메인 종료전, 스레드 종료가능토록 데몬스레드화. (이 예제에선 이로 인한 데이터 일관성 걱정 X)
                return thread;
            }
        }
```

## 성능측정2 : 비동기 호출 (Thread수 = 4) vs 비동기 호출 (Thread수 = Shops Size)

> Macbook air M2 기준 (Core CPU수 = 8개)

| ShopSize | AS-IS  | TO-BE  |
|----------|--------|--------|
| 1        | 1009ms | 1026ms |
| 4        | 1006ms | 1004ms |
| 8        | 2009ms | 1008ms |
| 16       | 3012ms | 1007ms |
| 32       | 5018ms | 1010ms |

[ AS-IS ]
> 비동기 호출 (Thread수 = 4)
- 따라서, 4개 상점까지는 1초내 완료.
- 그 이상은, 누군가의 스레드가 작업완료하여 스레드가 빌때 까지 대기.

[ TO-BE ]
> 비동기 호출 (Thread수 = Shops Size)
- 즉, 각 상점이 하나의 스레드에 할당되어 실행되도록 최적화.
- 따라서, 상점 개수가 늘어도 1초내 완료.

## Summary So far

> 컬렉션 계산을 병렬화 하는 방법에는 대표적으로 2가지가 있다.

1)컬렉션을 병렬스트림으로 전환

2)컬렉션반복하며, CF내부 연산으로 만드는 방법

- CF이용시, Custom Executor를 설정가능하여, 해당 프로그램에 맞는 최적화된 설정, 스레드풀 설정가능.
- 즉, CF이용시, 블록되는 계산이 없도록, 스레드풀 크기를 조절할 수 있다.

그렇다면, 언제 어느 방법을 써야 하나?

[ 병렬스트림 ]

> CPU bound한 연산 이용시.

- CPU bound한 연산 수행시에는, CPU 코어 수 이상의 스레드를 가질 필요가 없다.


[ CF ]

> IO bound(waiting block) 연산 이용시.

- CF이용시 커스컴 Executor를 설정가능. = 더 많은 유연성 제공.
  -  WAITING/COMPUTING 비율에 맞는 적절한 스레드 수 제공 가능.


# 여러 비동기 연산의 파이프라인화하여, 연산 결과를 하나로 합치는 방법

## 할인 서비스 추가

- 들어가기전에, 모든 상점이 "하나의 할인 서비스"를 사용한다고 가정.
- 해당 할인 서비스에서는, 서로 다른 할인율을 제공.

```java
public class Discount {
    public enum Code {
        NONE(0),
        SILVER(5),
        GOLD(10),
        PLATINUM(15),
        DIAMOND(20),
        ;

        private final int percentage;

        Code(int percentage) {
            this.percentage = percentage;
        }
    }
}
```

## 비동기 연산 파이프라인 만들기

TBD






# QnA

## Q. How to test 비동기 메서드?

## Q. 병렬스트림, 어떻게 내부적으로 병렬화가 일어나는가?

[ 지금 알고 있는 것]

- Fork-Join Frameworkwork 이용.
- Global  Fork-Join pool 이용.

[ 알고 싶은 것 ]

- CPU core수에 맞춰서, 자동으로 해당 코어수 만큼 스레드를 사용하는가?
- 아니라면, 몇개의 스레드를 사용하는가?
- Fork-Join Framework, 어떻게 이용하는가?

## Q. 병렬스트림, Custom ForkJoinPool 이용 불가능한가?

## Q. Fork-Join Thread Pool, default setting?(QueueCap, CorePoolNumber, MaxPoolNumber)

## Q. How to monitor, ForkJoinPool?


 