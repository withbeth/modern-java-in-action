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
refer `UsingThreadManually.java`