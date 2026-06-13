# Tambahkan aturan ProGuard khusus proyek di sini.
# Anda dapat mengontrol kumpulan file konfigurasi yang diterapkan menggunakan
# pengaturan proguardFiles di build.gradle.
#
# Untuk detail lebih lanjut, lihat
#   http://developer.android.com/guide/developing/tools/proguard.html

# Jika proyek Anda menggunakan WebView dengan JS, hapus tanda komentar pada baris berikut
# dan tentukan nama kelas yang memenuhi syarat sepenuhnya untuk kelas antarmuka JavaScript:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Hapus tanda komentar ini untuk menjaga informasi nomor baris guna
# menelusuri tumpukan debugging.
#-keepattributes SourceFile,LineNumberTable

# Jika Anda menyimpan informasi nomor baris, hapus tanda komentar ini untuk
# menyembunyikan nama file sumber asli.
#-renamesourcefileattribute SourceFile