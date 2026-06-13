# Business Intelligence (BI) Android Client Application

Aplikasi klien Android resmi untuk platform **Business Intelligence AI & Market Analysis Engine**. Aplikasi ini dibangun menggunakan **Kotlin & Jetpack Compose** dengan menerapkan arsitektur **MVVM (Model-View-ViewModel)** untuk menyajikan analisis pasar real-time dan analisis sentimen cerdas berbasis AI (FinBERT) ke dalam genggaman pengguna.

Aplikasi ini terhubung langsung dengan REST API dan WebSocket server produksi di `https://bi-api.bonodigital.biz.id`.

---

## 🚀 Fitur Utama Aplikasi

1. **IHSG Market Intelligence Overview**:
   - Dasbor pemantauan pergerakan bursa saham domestik Indonesia (IDX Blue Chip Watchlist: `BBCA`, `BBRI`, `BMRI`, `TLKM`, `ASII`, `GOTO`, `BREN`, `UNVR`).
   - Widget ringkasan sentimen rata-rata pasar bursa (`Bullish` / `Bearish` / `Netral`) dan rata-rata skor AI.
   - **Canvas Sentiment Bar Chart**: Grafik batang interaktif yang digambar kustom menggunakan Jetpack Compose Canvas untuk merepresentasikan skor FinBERT masing-masing saham utama IDX.

2. **Real-Time AI Stock Analysis**:
   - Fitur pencarian ticker saham global maupun lokal secara instan.
   - **Interactive Canvas Stock Chart**: Grafik visual kustom menggunakan Canvas Compose untuk melacak *7-day closing prices*. Dilengkapi fitur deteksi gestur sentuh (*drag gesture*) untuk menampilkan garis penunjuk dan tooltip harga/tanggal secara interaktif.
   - **AI Sentiment Analysis Card**: Visualisasi skor sentimen finansial FinBERT (skala 1-10) lengkap dengan *progress bar* dan interpretasinya.
   - **Live News Feed**: Scraping berita terkini dari CNBC Headlines secara real-time yang relevan dengan ticker saham.
   - **Smart Recommendations**: Panel aksi rekomendasi saham berdasarkan prioritas Tinggi/Sedang/Rendah disertai penjelasan taktis.
   - **Transparency & Explainability**: Penjelasan pembobotan nilai (40% harga historis, 60% sentimen FinBERT) serta pengungkapan driver utama sentimen positif dan negatif.

3. **Watchlist & Favorites (Lokal)**:
   - Menyimpan daftar saham favorit ke penyimpanan lokal menggunakan **Jetpack DataStore Preferences** secara efisien dan ringan.

4. **Dynamic API base URL configuration**:
   - Panel khusus di menu **Pengaturan** (Settings) untuk mengganti Base URL antara server hosting produksi HTTPS (`https://bi-api.bonodigital.biz.id`) dan server pengembangan lokal HTTP (`http://10.0.2.2:8000` / IP Lokal).
   - Switch toggle untuk mengaktifkan pembaruan WebSocket real-time.

---

## 🏗️ Arsitektur Aplikasi (Architecture & Tech Stack)

Aplikasi dirancang dengan arsitektur **MVVM (Model-View-ViewModel)** yang bersih, memastikan pemisahan tanggung jawab yang jelas antara data, logika bisnis, dan antarmuka visual.

```
studio.bonodigital.businessintelligence
├── MainActivity.kt               # Entry point Activity
├── data
│   ├── local
│   │   └── WatchlistStore.kt    # Jetpack Preferences DataStore
│   ├── model
│   │   └── DataModels.kt        # JSON GSON Serialization Models
│   ├── network
│   │   ├── ApiService.kt        # Retrofit HTTP Declarations
│   │   └── NetworkModule.kt     # Dynamic Retrofit Client & WebSocket Builder
│   └── repository
│       └── BiRepository.kt      # Repository pattern / Data source coordinator
├── ui
│   ├── components
│   │   └── CustomStockChart.kt  # Custom Canvas Chart Component
│   ├── navigation
│   │   └── MainNavigation.kt    # Bottom Bar Navigation & NavHost Setup
│   ├── screens
│   │   ├── IhsgDashboardScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── StockAnalysisScreen.kt
│   │   └── WatchlistScreen.kt
│   └── theme
│       ├── Color.kt             # Curated Financial Dark Theme Color Palette
│       ├── Theme.kt             # Material Theme Customization
│       └── Type.kt
```

### Tech Stack:
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI)
- **Networking**: Retrofit 2 (HTTP Client) & OkHttp 4 (WebSocket & Connection Logging)
- **JSON Parser**: Gson Converter
- **Local Persistence**: Jetpack Preferences DataStore
- **Navigation**: Jetpack Navigation Compose
- **Concurrency**: Kotlin Coroutines & Flow (Auto-refresh & WebSocket Stream handler)

---

## 🔧 Panduan Menjalankan Aplikasi (Local Development)

### Prasyarat:
- Android Studio (versi Ladybug / Jellyfish ke atas sangat direkomendasikan).
- Android SDK dengan **Minimum SDK 26** (Android 8.0 Oreo) dan **Target SDK 36** (Android 15).
- JDK 17 atau lebih baru terpasang di sistem.

### Langkah-langkah:
1. **Clone Repositori**:
   Buka terminal Android Studio lalu arahkan ke folder ini.
2. **Gradle Sync**:
   Biarkan Gradle mengunduh seluruh dependensi yang tertera di [libs.versions.toml](gradle/libs.versions.toml) dan [build.gradle.kts](app/build.gradle.kts).
3. **Jalankan Emulator / Hubungkan HP**:
   Aktifkan emulator Android atau hubungkan perangkat Android fisik Anda via USB Debugging.
4. **Build & Run**:
   Klik ikon tombol **Run** (segitiga hijau) di Android Studio.

---

## 🔒 Konfigurasi Keamanan Jaringan (HTTP Cleartext)

Demi mempermudah pengujian lokal oleh tim penguji capstone terhadap backend lokal (`http://localhost:8000`), berkas `AndroidManifest.xml` aplikasi ini telah dikonfigurasi secara aman untuk mengizinkan lalu lintas data non-HTTPS (*Cleartext Traffic*) menggunakan atribut berikut:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
</application>
```
*Catatan: Konfigurasi ini sangat berguna agar aplikasi di dalam emulator Android dapat mengakses server lokal komputer Anda via IP gateway khusus emulator `http://10.0.2.2:8000`.*

---

## 📊 Integrasi Endpoint Backend

Aplikasi Android ini berkomunikasi dengan FastAPI backend melalui antarmuka standardisasi data berikut:

| Fitur Android | Tipe Protokol | Target Endpoint / Fungsi |
|---|---|---|
| Dasbor IHSG IDX | HTTP GET | `/api/bi?ticker=<TICKER>&detail=summary` (Paralel) |
| Detail Saham | HTTP GET | `/api/bi?ticker=<TICKER>&detail=full` |
| Real-time Tick | WebSocket WS | `wss://bi-api.bonodigital.biz.id/ws` (Kirim payload `{"ticker":"...", "detail":"full"}`) |
| Berita CNBC | HTTP GET | `/api/news` |

---

## 🎨 Palet Desain UI/UX Premium
Aplikasi ini sepenuhnya mengadopsi identitas visual modern pasar finansial global:
- **Dark Mode Default** untuk meminimalkan ketegangan mata saat memantau pasar.
- **Neon Accent Colors**:
  - `#10B981` (Emerald Green) untuk indikasi bullish/peningkatan/sentimen positif.
  - `#EF4444` (Crimson Red) untuk indikasi bearish/penurunan/sentimen negatif.
  - `#F59E0B` (Warning Amber) untuk kondisi netral atau wait-and-see.

---

## 👥 Kredit Tim Pengembang
Aplikasi Android ini dibangun sebagai bagian dari proyek akhir kelompok **SC1** (Capstone Project):
- **Diaz** - Frontend Web Developer
- **Zamaruddin** - Backend AI Engineer
- **Bono Digital Studio Team** - Android Client Developer & Integrator

---
**Lisensi**: MIT License. Hak cipta dilindungi undang-undang kelompok Capstone SC1 2026.
