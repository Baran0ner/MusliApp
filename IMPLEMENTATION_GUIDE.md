# Islam Uygulaması — Uygulama Rehberi

Son güncelleme: Şubat 2026

---

## İçindekiler

1. [Genel Bakış](#genel-bakış)
2. [Mimari Yapı](#mimari-yapı)
3. [Ana Özellikler](#ana-özellikler)
4. [Dosya Haritası](#dosya-haritası)
5. [Detaylı Açıklamalar](#detaylı-açıklamalar)

---

## Genel Bakış

Bu Android uygulaması, Müslümanlar için kapsamlı bir İslami yardımcıdır ve aşağıdaki özellikleri sağlar:

- 🕌 **Namaz Vakitleri** — Konum veya ayarlardan belirlenen şehre göre gerçek zamanlı namaz saatleri
- 🔔 **Ezan Bildirimleri** — Müdüriye tam zaman alarmları, ses ve bildirim ile
- 🧭 **Kıble Yönü** — Manyetometre + İvmeölçer sensörü kullanarak pusula stil UI
- 📿 **Zikir Sayacı** — Offline Room Database, sayısal takip ve sıfırlama
- ✨ **Günlük İlham** — Her gün farklı Ayet/Hadis

---

## Mimari Yapı

Uygulama **Clean Architecture** + **MVVM** ile inşa edilmiştir:

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION (UI)                       │
│         HomeScreen, QiblaScreen, DhikrScreen, ...          │
│                    + ViewModels                             │
└────────────────────────────────────────┬────────────────────┘
                                         │
┌────────────────────────────────────────▼────────────────────┐
│                    DOMAIN LAYER                             │
│         Models, UseCases, Interfaces (Repositories)        │
└────────────────────────────────────────┬────────────────────┘
                                         │
┌────────────────────────────────────────▼────────────────────┐
│                  DATA LAYER                                 │
│    Repositories, Remote APIs, Local Database, Sensors      │
└─────────────────────────────────────────────────────────────┘
```

**Bağımlılık İnjeksiyonu:** Hilt (@HiltViewModel, @Singleton, @Binds, @Provides)

---

## Ana Özellikler

### 1. 🕌 Namaz Vakitleri Sistemi

**Akış:**
```
HomeScreen → HomeViewModel → GetPrayerTimesUseCase
                                      ↓
                            PrayerTimeRepository
                                      ↓
                        Retrofit (Aladhan API)
                                      ↓
                        PrayerTime Entity (Room Cache)
```

**Dosyalar:**
- `domain/model/PrayerTime.kt` — Veri modeli (imsak, fajr, dhuhr, vb.)
- `domain/usecase/prayer/GetPrayerTimesUseCase.kt` — Repository çağır, Network/Cache yönet
- `data/repository/PrayerTimeRepositoryImpl.kt` — Retrofit + Room logic
- `data/remote/AladhanApi.kt` — REST endpoint (https://api.aladhan.com/)

**Dinamik Takvim:** `Calendar.getInstance().get(Calendar.DAY_OF_YEAR)` ile her gün yenilenir.

---

### 2. 🔔 Ezan Bildirimleri & Foreground Service

**3 Seviye İzin Yönetimi:**

| İzin | API | Kontrol |
|------|-----|---------|
| Konum (GPS) | Tüm | init'te senkron check |
| Bildirim POST | 33+ | Sequential flow |
| Tam Alarm | 31+ | Settings açılır |

**Akış:**
```
AlarmManager (Exact Alarm)
        ↓
PrayerAlarmReceiver (BroadcastReceiver)
        ↓
EzanForegroundService (startForegroundService)
        ↓
[startForeground + Notification]
        ↓
MediaPlayer.play(R.raw.ezan_sesi)
        ↓
Notification Button "Durdur" → ACTION_STOP → stopSelf()
```

**Dosyalar:**
- `notification/AlarmScheduler.kt` — `setExactAndAllowWhileIdle()` + `canScheduleExactAlarms()` check
- `notification/PrayerAlarmReceiver.kt` — Servis başlatır (`ContextCompat.startForegroundService`)
- `services/EzanForegroundService.kt` — MediaPlayer + Foreground Notification
- `notification/NotificationHelper.kt` — 7 adet per-prayer kanal (Prayer.entries.forEach)
- `res/raw/ezan_sesi.mp3` — Placeholder (gerçek MP3 ile değiştirin)

**Android 14+ Uyumluluğu:**
```kotlin
startForeground(NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
```

---

### 3. 🧭 Kıble Yönü (Pusula)

**Sensör Kalibrasyonu:**
```
SensorManager.TYPE_ACCELEROMETER (çekim)
        +
SensorManager.TYPE_MAGNETIC_FIELD (manyetik alan)
        ↓
getRotationMatrix() [4×4 matris]
        ↓
getOrientation() [azimuth radyan]
        ↓
Derece cönütüm + Low-Pass Filter (alpha=0.15f)
        ↓
CompassData(azimuth, qiblaAngle, bearingToQibla)
```

**Kıble Hesaplaması (Great Circle):**
```kotlin
// Kaaba koordinatları (sabit)
KAABA_LAT = 21.422487, KAABA_LON = 39.826206

// Formül: Spherical Trigonometry
θ = atan2(sin(Δλ)·cos(φ₂), cos(φ₁)·sin(φ₂) − sin(φ₁)·cos(φ₂)·cos(Δλ))

// Normalize: 0-360°
result = (bearing + 360f) % 360f
```

**Dosyalar:**
- `services/CompassTracker.kt` — Flow-based sensor fusion
- `utils/QiblaCalculator.kt` — Pure math object (testlenebilir)
- `presentation/qibla/QiblaViewModel.kt` — StateFlow + collectLatest
- `presentation/qibla/QiblaScreen.kt` — Canvas compass (2 katman)

**Canvas Rendering:**
```
Layer 1 (Dönen): Pusula halkası (4 yön + renkli iğne)
Layer 2 (Sabit): Kıble oku (bearingToQibla açısında)
```

---

### 4. 📿 Zikir Sayacı (Offline)

**Room Database Akışı:**
```
DhikrViewModel → GetDhikrListUseCase
        ↓
DhikrRepository (Interface)
        ↓
DhikrRepositoryImpl
        ↓
DhikrDao (Room @Dao)
        ↓
IslamDatabase (SQLite)
```

**Sayaç Mantığı:**
```
[1] Seed (init): 33 adet pre-defined dhikir yüklenir
[2] Increment: currentCount++ (IncrementDhikrUseCase)
[3] Reset: currentCount = 0 (ResetDhikrUseCase)
[4] List: Flow<List<Dhikr>> (GetDhikrListUseCase)
```

**Dosyalar:**
- `data/local/entity/DhikrEntity.kt` — @Entity (id, arabicName, turkishName, currentCount)
- `data/local/dao/DhikrDao.kt` — @Insert, @Update, @Delete, @Query
- `data/local/database/IslamDatabase.kt` — @Database (version = 1)
- `di/DatabaseModule.kt` — @Provides @Singleton

---

### 5. ✨ Günlük Ayet/Hadis (Daily Motivation)

**Seçim Mekanizması:**
```
dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
selectedIndex = dayOfYear % quotes.size

// Saat 00:00'de otomatik döner, benzersiz ve deterministik
```

**13 Statik Öğe:**
```
Ayet 1-7:  Bakara 255, 286; İnşirah 5-6; Talâk 3; Ra'd 28; Zümer 53; Bakara 152
Hadis 8-13: Müslüman tanımı, Devamlı amel, Kolaylaştırın, Gülümseme, Öfkeye hâkim, Dünya tarlası
```

**Dosyalar:**
- `domain/model/DailyQuote.kt` — Data class (text, source, type enum)
- `data/local/DailyQuoteDataSource.kt` — Statik List
- `domain/usecase/quote/GetDailyQuoteUseCase.kt` — dayOfYear % size logic
- `presentation/home/HomeViewModel.kt` — init'te senkron çağır
- `presentation/home/HomeScreen.kt` — DailyQuoteCard composable

**UI:**
```
┌─────────────────────────────────────┐
│ [ Ayet ]  ← Tür etiketi            │
│                                     │
│ "...metin..."                       │
│                                     │
│            — Kaynakbilgisi →       │
└─────────────────────────────────────┘

Renk: Ayet → secondaryContainer (yeşil)
      Hadis → tertiaryContainer (turuncu)
```

---

## Dosya Haritası

### Oluşturulan Dosyalar (YENİ)

```
app/src/main/
├── java/com/example/islam/
│   ├── domain/
│   │   ├── model/
│   │   │   └── DailyQuote.kt                      ✨ Günlük Quote modeli
│   │   └── usecase/
│   │       └── quote/
│   │           └── GetDailyQuoteUseCase.kt        ✨ Quote seçim logici
│   ├── data/
│   │   └── local/
│   │       └── DailyQuoteDataSource.kt            ✨ 13 statik quote
│   ├── services/
│   │   ├── EzanForegroundService.kt               🔔 MediaPlayer servisi
│   │   └── CompassTracker.kt                      🧭 Sensor fusion
│   ├── utils/
│   │   └── QiblaCalculator.kt                     🧭 Kıble math object
│   └── presentation/
│       ├── qibla/
│       │   ├── QiblaScreen.kt                     🧭 Pusula UI
│       │   └── QiblaViewModel.kt                  🧭 Sensor state
│       └── home/
│           └── (güncellemeler aşağı bakın)
│
└── res/
    └── raw/
        └── ezan_sesi.mp3                          🔔 Placeholder
```

### Güncellenen Dosyalar (MODİFİYE)

```
app/src/main/
├── java/com/example/islam/
│   ├── presentation/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt                      ✨ DailyQuoteCard eklendi
│   │   │   └── HomeViewModel.kt                   ✨ getDailyQuoteUseCase inject
│   │   ├── qibla/ (yenisi)
│   │   │   ├── QiblaScreen.kt
│   │   │   └── QiblaViewModel.kt
│   │   └── dhikr/
│   │       └── DhikrViewModel.kt                  🔧 Cast kaldırıldı
│   ├── notification/
│   │   ├── PrayerAlarmReceiver.kt                 🔧 Service başlatır
│   │   ├── NotificationHelper.kt                  🔧 7 kanal (forEach)
│   │   └── AlarmScheduler.kt                      🔧 canScheduleExactAlarms()
│   ├── di/
│   │   ├── LocationModule.kt                      ✨ YENİ
│   │   └── NetworkModule.kt                       🔧 BuildConfig.DEBUG
│   ├── data/
│   │   ├── location/
│   │   │   └── DefaultLocationTracker.kt          ✨ YENİ
│   │   └── repository/
│   │       └── DhikrRepositoryImpl.kt              🔧 Override eklemeleri
│   ├── domain/
│   │   ├── utils/
│   │   │   └── LocationTracker.kt                 ✨ YENİ
│   │   ├── repository/
│   │   │   └── DhikrRepository.kt                 🔧 İmza güncellemesi
│   │   └── model/
│   │       └── Prayer.kt                          (unchanged)
│   ├── core/
│   │   └── util/
│   │       ├── DateUtil.kt                        🔧 Typo düzeltme
│   │       └── Extensions.kt                      ✨ YENİ
│   └── MainActivity.kt                            🔧 Qibla nav item
│
├── AndroidManifest.xml                            🔧 İzinler + Service decl
│
└── gradle/
    └── libs.versions.toml                         🔧 Kotlin/KSP versions
```

---

## Detaylı Açıklamalar

### A. HomeScreen Ana Düzeni

```
Column {
    Spacer(16dp)
    Text("بِسْمِ اللَّهِ...")                    ← Başlık
    Text(todayDate)                              ← Tarih
    Text(hijriDate)                              ← Hicri tarih

    Spacer(16dp)
    ┌─────────────────────────────────────┐
    │ DailyQuoteCard(quote)               │   ← ✨ YENİ
    │                                     │
    │ "...metin..."                       │
    │  — Kaynak                           │
    └─────────────────────────────────────┘

    Spacer(16dp)

    when (state) {
        isLoading → CircularProgressIndicator
        error != null → ErrorCard + "Tekrar Dene" button
        nextPrayer != null → {
            NextPrayerCard(...)
            PrayerSummaryCard(...)
        }
    }
}
```

**Durum Yönetimi:**
- `HomeUiState.dailyQuote` → ViewModel init'te set edilir (senkron, network yok)
- `HomeUiState.permissionsGranted` → Sequential permission flow kontrolü
- `HomeUiState.nextPrayer` → Countdown ticker (1 saniye interval)

---

### B. EzanForegroundService İş Akışı

**1. BroadcastReceiver tetiklenir (AlarmManager):**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    val serviceIntent = EzanForegroundService.buildStartIntent(...)
    ContextCompat.startForegroundService(context, serviceIntent)  // API 26+
}
```

**2. Service başlar:**
```kotlin
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
        ACTION_START → {
            val notification = buildNotification(...)
            if (Build.VERSION.SDK_INT >= UPSIDE_DOWN_CAKE) {
                startForeground(id, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(id, notification)
            }
            playEzan()
        }
        ACTION_STOP → stopEzan()
    }
    return START_NOT_STICKY
}
```

**3. MediaPlayer kurulumu:**
```kotlin
val afd = resources.openRawResourceFd(R.raw.ezan_sesi)
mediaPlayer = MediaPlayer().apply {
    setAudioAttributes(Builder()
        .setUsage(USAGE_ALARM)        // ← Ses zili kanalını delme
        .setContentType(CONTENT_TYPE_MUSIC)
        .build()
    )
    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    prepare()
    setOnCompletionListener { stopEzan() }
    start()
}
```

**4. Notification:**
- Content Intent: ACTION_STOP + MainActivity aç (getActivities)
- Action Button "Durdur": ACTION_STOP service intent
- Category: CATEGORY_ALARM
- Visibility: VISIBILITY_PUBLIC (kilit ekranında göster)

**5. Temizleme:**
```kotlin
override fun onDestroy() {
    mediaPlayer?.stop()
    mediaPlayer?.release()
    super.onDestroy()
}
```

---

### C. Kıble Yönü Hesaplaması

**QiblaCalculator.kt — Pure Object (testlenebilir):**

```kotlin
object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val φ1 = userLat.toRadians()
        val φ2 = KAABA_LAT.toRadians()
        val Δλ = (KAABA_LON - userLng).toRadians()

        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad).toFloat()

        return (bearingDeg + 360f) % 360f
    }
}
```

**CompassTracker.kt — Sensor Fusion:**

```kotlin
fun track(userLat: Double, userLon: Double): Flow<CompassData> = callbackFlow {
    val listener = object : SensorEventListener {
        private var accelData = FloatArray(3)
        private var magneticData = FloatArray(3)
        private var rotationMatrix = FloatArray(9)
        private var orientation = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                TYPE_ACCELEROMETER → {
                    accelData = lowPassFilter(event.values, accelData)
                }
                TYPE_MAGNETIC_FIELD → {
                    magneticData = lowPassFilter(event.values, magneticData)
                }
            }

            if (getRotationMatrix(rotationMatrix, null, accelData, magneticData)) {
                getOrientation(rotationMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val qibla = QiblaCalculator.calculateQiblaDirection(userLat, userLon)
                val bearing = (qibla - azimuth + 360f) % 360f

                trySend(CompassData(azimuth, qibla, bearing))
            }
        }
    }

    sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
    awaitClose { sensorManager.unregisterListener(listener) }
}

private fun lowPassFilter(input: FloatArray, output: FloatArray): FloatArray {
    val alpha = 0.15f
    output[0] = alpha * input[0] + (1 - alpha) * output[0]
    output[1] = alpha * input[1] + (1 - alpha) * output[1]
    output[2] = alpha * input[2] + (1 - alpha) * output[2]
    return output
}
```

---

### D. Permission Flow (Sequential)

```
init {
    permissionsGranted = areAllPermissionsGranted()  ← Senkron check
}

if (!permissionsGranted) {
    HomePermissionFlow()  ← Adım adım izin iste
    return
}

// ── Step by Step ───────────────────────────────
Step 1: LOCATION
└─→ HasLocationPermission? YES → Step 2

Step 2: NOTIFICATION (Android 13+)
└─→ HasNotificationPermission? YES → Step 3

Step 3: EXACT_ALARM (Android 12+)
└─→ CanScheduleExactAlarms? YES → Step 4 (DONE)

Step 4: DONE
└─→ onPermissionsGranted() → observePreferences()
```

**Her Step'in UI:**
```kotlin
PermissionCard(
    emoji = "📍",
    title = "Konum İzni Gerekli",
    description = "...",
    buttonText = "Konuma İzin Ver",
    onRequest = { locationLauncher.launch(...) }
)
```

---

### E. DailyQuote Seçim Mantığı

```kotlin
class GetDailyQuoteUseCase @Inject constructor(
    private val dataSource: DailyQuoteDataSource
) {
    operator fun invoke(): DailyQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val quotes = dataSource.quotes
        return quotes[dayOfYear % quotes.size]  // ← Deterministik
    }
}
```

**Avantajlar:**
- ✅ Network yok (senkron)
- ✅ Veritabanı sorgusu yok
- ✅ Aynı gün hep aynı öğe
- ✅ Testlenebilir
- ✅ Sunucu gerekmiyor

---

## Hilt Bağımlılık Injeksiyonu

```kotlin
// ── Module'ler ───────────────────────────────────

@Module @InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds @Singleton
    abstract fun bindLocationTracker(impl: DefaultLocationTracker): LocationTracker

    companion object {
        @Provides @Singleton
        fun provideFusedLocationClient(...): FusedLocationProviderClient
    }
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideIslamDatabase(context: Context): IslamDatabase
}

// ── ViewModel Injection ───────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val getNextPrayerUseCase: GetNextPrayerUseCase,
    private val getDailyQuoteUseCase: GetDailyQuoteUseCase,    // ← ✨
    private val prefsDataStore: UserPreferencesDataStore,
    private val locationTracker: LocationTracker,              // ← ✨
    @ApplicationContext private val appContext: Context
) : ViewModel()

// ── Service Injection ─────────────────────────────

@Singleton
class CompassTracker @Inject constructor(
    @ApplicationContext private val context: Context
)
```

---

## Android Manifest İzinleri

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />       <!-- API 33+ -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />     <!-- API 31+ -->
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />          <!-- API 31+ -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />        <!-- API 28+ -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" /> <!-- API 29+ -->

<!-- Manifest içinde Service bildirimi -->
<service
    android:name=".services.EzanForegroundService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback" />
```

---

## Veri Akışı Şemaları

### Namaz Vakitleri (ilk load)

```
HomeScreen
    ↓
onPermissionsGranted() [Permission Flow tamamlanınca]
    ↓
observePreferences() [DataStore collect]
    ↓
loadPrayerTimes(prefs)
    ↓
GetPrayerTimesUseCase(city, country, method)
    ↓
PrayerTimeRepository.getPrayerTimes()
    ↓
AladhanApi.getPrayerTimes() [Retrofit call]
    ↓
Cache in Room (PrayerTimeEntity)
    ↓
HomeViewModel._uiState.update { copy(prayerTime, nextPrayer) }
    ↓
HomeScreen recompose → NextPrayerCard göster
```

### Countdown Ticker (her saniye)

```
init { startCountdownTicker() }
    ↓
while (true) {
    delay(1000L)
    nextPrayer.millisUntil -= 1000L
    _uiState.update { copy(countdownText = format(remaining)) }
    ↓
    HomeScreen recompose → Countdown güncellenir
}
```

### Ezan Sesi (alarm triggered)

```
AlarmManager.setExactAndAllowWhileIdle()
    ↓
[Saat gelir]
    ↓
PrayerAlarmReceiver.onReceive()
    ↓
ContextCompat.startForegroundService(EzanForegroundService)
    ↓
EzanForegroundService.onStartCommand(ACTION_START)
    ↓
startForeground(notification)
    ↓
MediaPlayer.prepare() + start()
    ↓
[Ses çalar]
    ↓
Notification "Durdur" button → ACTION_STOP
    ↓
stopEzan() → stopForeground() + stopSelf()
```

---

## Kodlama Kuralları

### 1. Dosya Organizasyonu

```
app/src/main/java/com/example/islam/
├── di/                    ← Hilt modules (@Module, @Provides, @Binds)
├── core/                  ← Utils, Constants, Extensions
├── domain/
│   ├── model/            ← Data classes (Religion logic yok, pure model)
│   ├── repository/       ← Interfaces (contract definitions)
│   └── usecase/          ← Business logic (suspend/operator fun invoke)
├── data/
│   ├── remote/           ← Retrofit APIs
│   ├── local/            ← Room DAOs, Database, DataSource
│   └── repository/       ← Implementations
├── presentation/
│   ├── [feature]/        ← Screens, ViewModels
│   └── [feature]/        ← ...
├── services/             ← Android Services, Sensors, Trackers
└── notification/         ← AlarmManager, BroadcastReceiver, Notifications
```

### 2. Naming Conventions

| Tür | Örnek |
|-----|-------|
| UseCase Class | `GetPrayerTimesUseCase`, `IncrementDhikrUseCase` |
| UseCase Method | `operator fun invoke()` |
| Repository Interface | `PrayerTimeRepository` |
| Repository Impl | `PrayerTimeRepositoryImpl` |
| ViewModel | `HomeViewModel` |
| State Class | `HomeUiState` |
| Composable (Screen) | `HomeScreen()` |
| Composable (Component) | `NextPrayerCard()`, `DailyQuoteCard()` |
| DataSource | `DailyQuoteDataSource` |
| Tracker/Service | `CompassTracker`, `EzanForegroundService` |

### 3. StateFlow Pattern

```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val data: Model? = null,
    val error: String? = null
)

class ViewModel {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun updateState(block: (UiState) -> UiState) {
        _uiState.update { block(it) }
    }
}
```

### 4. UseCase Şablonu

```kotlin
class MyUseCase @Inject constructor(
    private val repository: MyRepository
) {
    suspend operator fun invoke(param: Type): Result<Data> {
        return repository.fetchData(param)
    }
}

// Senkron UseCase
class GetDailyQuoteUseCase @Inject constructor(
    private val dataSource: DailyQuoteDataSource
) {
    operator fun invoke(): DailyQuote {
        // Senkron, network yok
        return dataSource.getQuoteForToday()
    }
}
```

---

## Test Edilebilirlik

### Unit Tests Yazma

```kotlin
class QiblaCalculatorTest {
    @Test
    fun testCalculateQiblaDirection() {
        val calculator = QiblaCalculator

        // İstanbul (41.0082, 28.9784) → Kıble ~151.6°
        val result = calculator.calculateQiblaDirection(41.0082, 28.9784)

        assertTrue(result in 145f..160f)
    }
}

class GetDailyQuoteUseCaseTest {
    @Test
    fun testQuoteConsistency() {
        val useCase = GetDailyQuoteUseCase(mockDataSource)

        val quote1 = useCase()
        val quote2 = useCase()  // Aynı gün

        assertEquals(quote1, quote2)
    }
}
```

---

## Yaygın Sorunlar & Çözümler

| Sorun | Çözüm |
|-------|-------|
| AlarmManager çalışmıyor | `canScheduleExactAlarms()` check yap, Permission ver |
| Ezan sesi başlatılamıyor | `R.raw.ezan_sesi.mp3` dosyasını `res/raw/` içine yerleştir |
| Pusula hızlı sallantı yapıyor | `lowPassFilter` alpha değerini arttır (0.15 → 0.3) |
| Foreground Service notification yok | 5 saniye içinde `startForeground()` çağrıl |
| Hilt injection başarısız | `@HiltAndroidApp` ApplicationClass'ını kontrol et |
| Permission flow sonsuz döngü | `PermissionStep.DONE` kodu `LaunchedEffect` içinde kontrol et |

---

## Sonraki Adımlar

- [ ] Real `ezan_sesi.mp3` dosyası ekle (res/raw/)
- [ ] Settings Screen oluştur (şehir, hesaplama metodu seçimi)
- [ ] Zikir UI'ında sesli ve haptik feedback ekle
- [ ] Prayer Times API'de offline mode (cached data)
- [ ] Widget oluştur (Quick prayer time view)
- [ ] Notification action: "Namaz kıldım" button

---

## İçinde Kullanılan Teknolojiler

```
┌─ Frontend ──────────────────┐
│ Jetpack Compose             │
│ Navigation Compose          │
│ Material 3 Design System    │
└─────────────────────────────┘

┌─ Architecture ──────────────┐
│ Clean Architecture          │
│ MVVM Pattern                │
│ Repository Pattern          │
│ UseCase Pattern             │
└─────────────────────────────┘

┌─ State Management ──────────┐
│ Kotlin Coroutines           │
│ Flow / StateFlow            │
│ ViewModel                   │
│ DataStore Preferences       │
└─────────────────────────────┘

┌─ Data ──────────────────────┐
│ Retrofit 2 (REST API)       │
│ Room Database (SQLite)      │
│ JSON (Gson converter)       │
└─────────────────────────────┘

┌─ Hardware ──────────────────┐
│ SensorManager               │
│ AlarmManager                │
│ FusedLocationProviderClient │
│ MediaPlayer                 │
└─────────────────────────────┘

┌─ DI ───────────────────────┐
│ Hilt / Dagger 2             │
└─────────────────────────────┘
```

---

**Belge Son Güncelleme:** Şubat 2026
**Proje Başlangıç:** [Tarih]
**Bakım Sorumlusu:** [Ad]

