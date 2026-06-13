// File build tingkat atas tempat Anda dapat menambahkan opsi konfigurasi umum untuk semua subproyek/modul.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}