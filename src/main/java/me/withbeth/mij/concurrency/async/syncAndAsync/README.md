# 동기API vs 비동기API

## What we want to do ?
- 다음 시그니처를 갖는 두 메서드 결과를 합치고 싶다.
- f와 g를, 별도 CPU코어로 실행해, 시간을 최대 max(f, g)로 줄이고 싶다.
- 두 메서드는 현재 '물리적 결과'를 반환하므로 동기API
```java
int f(int x);
int g(int x);
```

## OptionA : 명시적으로 스레드 사용
> refer `UsingThreadManually.java`
```java
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
```

## OptionB : 스레드풀 이용
> refer `UsingExecutorService.java`
```java
int x = 1;

ExecutorService executorService = Executors.newFixedThreadPool(2);
Future<Integer> y = executorService.submit(() -> ExternalAPIs.f(x));
Future<Integer> z = executorService.submit(() -> ExternalAPIs.g(x));

final int result = y.get() + z.get(); // blocking
System.out.println("result = " + result);

executorService.shutdown();
```
- 쓰레드 대신, ExecutorService이용하여 태스크 제출하고 Future형식으로 반환받는다.
- 코드는 더 단순화되었지만, 아직 submit같은 불필요한 코드가 남아있다.
- 스트림 내부 반복 병렬화처럼, 조금 더 추상화 가능하지 않을까? 

## OptionC : `비동기API로 API를 변경해서 해결`

- `Future형식 API`
 
  - API의 메서드 시그니처를 `Future<Integer> f(int x)`로 변환
  - refer `UsingFutureTask.java`
  
- `리액티브 형식 API`
 
  - API의 메서드 시그니처를, `void f(int x, IntConsumer resultConsumer)`로 변환.
  - 즉, `콜백` 형식의 프로그래밍 이용.
  - f()에서, 결과가 준비되면, 람다를 호출하는 태스크를 만드는 것이 포인트.
  - 비동기이여야하기에, 바디 실행 -> 결과 준비시 람다 호출하는 태스크 작성 -> 즉시 반환 하는 형식으로 작동.
  - refer `UsingCallback.java`

- `리액티브 형식의 단점`
  - 콜백이 여러번 호출 될 수 있다 (리액티브는 일련의 이벤트(스트림)처리 용이므로.)
  - 호출 합계를 정확히 출력되지 않고, 상황에 따라 먼저 계산된 결과를 출력한다.
   
  - 즉, 우리가 원하는 결과를 위해서는 일련의 스트림 처리용 리액티브보단, Future형식 이용 필요.
  - 우리가 원했던 결과 : `각 함수들은 한번만 호출되며, 호출합계는 정확히 1번 출력되어야 한다.`

## Summary So far

정리하자면,
- Future : 일회성 값 처리에 적합
- 리액티브 : 일련의 스트림 처리에 적합

그리고, 위 방식 이용시, 명시적으로 스레드를 처리하는 코드에 비해, 
더 높은 수준의 추상화를 통해 사용코드를 더 단순하게 만들 수 있다.

특히, 비동기API이므로, `blocking 작업(Computation or IO waiting)`에 적절히 활용시,
서버의 리소스를 효율적으로 사용할 수 있다.



## OptionD : CompletableFuture
> CompletableFuture + Combinator 이용해 get()에서 블로킹 되지 않고 연산 수행 가능
 
> refer `UsingCompletableFuture.java`

```java
int x = 1;
ExecutorService executorService = Executors.newFixedThreadPool(3);

CompletableFuture<Integer> a = new CompletableFuture<>();
CompletableFuture<Integer> b = new CompletableFuture<>();
// 두 작업을 합치는(조합하는) 별도 태스크 이용
// - Future a, b의 결과를 알지 못하는 상태에서, 두 연산이 끝났을때 실행할 태스크 작성.
CompletableFuture<Integer> c = a.thenCombine(b, (y, z) -> y + z);

executorService.submit(() -> a.complete(ExternalAPIs.f(x)));
executorService.submit(() -> b.complete(ExternalAPIs.g(x)));

// 다른 두 작업이 끝날때까지 실행 X (=먼저 시작해서 blocking하지 않는다)
Integer result = c.get();
System.out.println("result = " + result);

executorService.shutdown();
```