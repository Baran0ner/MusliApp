# Performance Gate

Bu modül iki amaç için kullanılır:

1. `Baseline Profile` üretmek (`BaselineProfileGenerator`)
2. `Macrobenchmark` ile startup/frame metriklerini ölçmek (`StartupBenchmark`)

## Çalıştırma

1. Baseline profile üret:

```bash
./gradlew :app:generateBaselineProfile
```

2. Macrobenchmark + gate çalıştır:

```bash
./gradlew :benchmark:verifyReleasePerformance
```

3. Tek komutta release gate:

```bash
./gradlew performanceGate
```

## Eşikler (isteğe bağlı)

Varsayılan eşikler:

- startup median: `1200ms`
- frame overrun median: `16ms`

Komut satırından override:

```bash
./gradlew :benchmark:verifyReleasePerformance \
  -Pperf.startup.max.median.ms=1000 \
  -Pperf.frame.max.median.ms=12
```
