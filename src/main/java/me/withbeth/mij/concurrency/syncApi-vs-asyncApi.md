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


