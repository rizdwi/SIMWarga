# SIMWarga - Sistem Informasi Manajemen Warga RT/RW (Offline & Excel Integrated)

<p align="center">
  <b>Aplikasi Android native offline-first untuk pendataan Kartu Keluarga (KK) dan Nomor Induk Kependudukan (NIK) warga tingkat RT/RW, terintegrasi penuh dengan Microsoft Excel (.xlsx).</b>
</p>

---

## 📌 Sekilas Aplikasi

**SIMWarga** dirancang khusus untuk mempermudah tugas pengurus RT/RW, kader dasawisma, dan panitia lingkungan dalam mengelola data administrasi kependudukan. Aplikasi ini bekerja **100% offline tanpa membutuhkan koneksi internet**, menjaga kerahasiaan data kependudukan warga tetap tersimpan secara aman di dalam perangkat pengguna.

Aplikasi mengadopsi format acuan resmi **Buku Induk Pendaftaran Penduduk / Daftar Jiwa Usik RT/RW**, sehingga data dari spreadsheet Excel lama dapat langsung diimpor dan diekspor kembali dengan tata letak kolom yang sama persis tanpa perlu konversi ulang.

---

## ✨ Fitur Utama

### 1. 📊 Ringkasan Statistik Warga (Dashboard)
- Tampilan realtime metrik kependudukan: **Total KK**, **Total Jiwa**, **Jumlah Laki-laki**, dan **Jumlah Perempuan**.
- Statistik diperbarui secara otomatis menggunakan reactive `Flow` & `StateFlow` setiap kali terjadi perubahan data.

### 2. 👨‍👩‍👧‍👦 Manajemen Kartu Keluarga & Anggota
- **Relasi One-to-Many Otomatis**: Setiap anggota keluarga terhubung langsung dengan nomor KK kepala keluarganya melalui relasi Room `@Relation` dan *Cascade Delete*.
- **Pencegahan Duplikasi NIK & KK**: Sistem memvalidasi tepat 16 digit angka serta mencegah duplikasi nomor NIK pada database.
- Formulir lengkap sesuai standar Disdukcapil (Tempat/Tanggal Lahir, Status Hubungan, Agama, Pendidikan, Pekerjaan, Status Perkawinan, Kewarganegaraan).

### 3. 🔍 Pencarian Realtime Cepat
- Pencarian multi-kriteria berbasis nama lengkap, NIK, No. KK, maupun alamat rumah.
- Hasil pencarian disajikan terstruktur per Kartu Keluarga.

### 4. 📑 Integrasi Penuh Excel (.xlsx & .csv)
- **Format Acuan Standar RT/RW**: Struktur tabel disesuaikan dengan format Buku Induk Warga (Header wilayah baris 1-2, header bertingkat baris 4-5, kolom B s/d U).
- **Kompatibilitas Bolak-Balik (Round-Trip)**: Berkas yang diekspor dari aplikasi memiliki urutan dan nama kolom yang sama persis dengan berkas yang diimpor.
- **Grouping Otomatis**: Membaca baris Kepala Keluarga vs Anggota Keluarga secara cerdas dan mengelompokkannya di bawah No. KK masing-masing.
- **Unduh Format Contoh**: Tersedia tombol untuk mengunduh template spreadsheet Excel kosong beserta contoh isian 2 baris keluarga.
- **Zero-Dependency OpenXML Parser**: Dibangun menggunakan pustaka standar Android/Java (`ZipOutputStream`, `ZipInputStream`, dan `SAXParser`) sehingga ukuran aplikasi tetap sangat ramping (~6.4 MB) tanpa dependensi berat seperti Apache POI.

### 5. 🔒 Keamanan & Privasi Data
- **Tanpa Izin Internet**: Berkas `AndroidManifest.xml` tidak memuat `android.permission.INTERNET`. Data kependudukan warga dijamin tidak akan dikirim keluar perangkat.
- **Kunci Keamanan PIN**: Dilengkapi kunci PIN 4 hingga 6 digit dengan enkripsi satu arah SHA-256.

---

## 📋 Struktur Kolom Tabel Excel (18 Kolom Standar)

Format spreadsheet mengikuti susunan resmi Buku Induk Warga RT/RW:

| Kolom | Nama Header Baris 4 | Sub-Header Baris 5 | Keterangan & Contoh |
|:---:|---|---|---|
| **B** | `No.` | - | Nomor urut KK (terisi pada baris Kepala Keluarga) |
| **C** | `No.      Urut` | - | Nomor urut seluruh jiwa warga (1, 2, 3...) |
| **D** | `KEPALA KELUARGA` | - | Nama lengkap Kepala Keluarga (mis. `Budi Santoso`) |
| **E** | `ANGGOTA KELUARGA` | - | Nama lengkap anggota (mis. `Siti Rahmawati`) |
| **F** | `JENIS KELAMIN` | - | `L` (Laki-laki) atau `P` (Perempuan) |
| **G** | `TEMPAT` | `LAHIR` | Tempat lahir (mis. `Kota Depok`) |
| **H** | `TGL/BLN/THN` | `LAHIR` | Format tanggal `DD/MM/YYYY` (mis. `15/08/1990`) |
| **I** | `L` | - | Indikator angka `1` untuk laki-laki |
| **J** | `P` | - | Indikator angka `1` untuk perempuan |
| **K** | `THN` | - | Tahun lahir otomatis (mis. `1979`) |
| **L** | `UMUR` | - | Umur dalam tahun (mis. `44`) |
| **M** | `STATUS` | - | Status perkawinan (`Kawin`, `Belum Kawin`, `Cerai`) |
| **N** | `Agama` | - | Agama (`Islam`, `Kristen`, `Katolik`, dll) |
| **O** | `Pendidikan Terakhir` | - | Pendidikan (`SLTA`, `SD`, `S1`, dll) |
| **P** | `Pekerjaan` | - | Pekerjaan (`Karyawan Swasta`, `Ibu Rumah Tangga`, dll) |
| **Q** | `Nomor` | `KTP` | **NIK warga (16 digit angka)** |
| **R** | `Nomor` | `KK` | **Nomor Kartu Keluarga (16 digit angka)** |
| **S** | `KETERANGAN` | - | Catatan tambahan (mis. `BARU PINDAH`) |
| **T** | `ORANG TUA` | `AYAH` | Nama orang tua ayah |
| **U** | `ORANG TUA` | `IBU` | Nama orang tua ibu |

---

## 🛠️ Tech Stack & Arsitektur

- **Bahasa**: Kotlin 1.9.22
- **Arsitektur**: MVVM (Model - View - ViewModel) + Repository Pattern
- **Database Lokal**: SQLite melalui Android Room Database 2.6.1 + KSP
- **UI & Komponen**: Material Design 3, ViewBinding, AndroidX Navigation Component
- **Spreadsheet Engine**: OpenXML SAX Parser & Zip Streams mandiri (Standard Java Runtime)
- **Target OS**: Android 7.0 Nougat (API 24) s/d Android 14 (API 34)

```
SIMWarga/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml (Bersih tanpa INTERNET permission)
│   │   ├── java/com/simwarga/
│   │   │   ├── data/
│   │   │   │   ├── model/ (Keluarga, Anggota, KeluargaWithAnggota)
│   │   │   │   ├── db/ (KeluargaDao, AnggotaDao, AppDatabase)
│   │   │   │   └── repository/ (WargaRepository)
│   │   │   ├── ui/
│   │   │   │   ├── dashboard/ (Ringkasan statistik)
│   │   │   │   ├── keluarga/ (Daftar, detail, dan form KK)
│   │   │   │   ├── anggota/ (Form data anggota keluarga)
│   │   │   │   ├── cari/ (Pencarian realtime)
│   │   │   │   └── backup/ (Impor/Ekspor Excel & unduh template)
│   │   │   └── util/
│   │   │       ├── ExcelHelper.kt (Parser & Generator OpenXML .xlsx)
│   │   │       ├── ValidationHelper.kt (Validasi 16 digit NIK/KK & PIN)
│   │   │       ├── PinHelper.kt (Keamanan hash SHA-256)
│   │   │       └── DateHelper.kt (Format & parser tanggal Indonesia)
│   │   └── res/ (Layouts, themes, drawables, strings, navigation graph)
│   └── src/test/ (Unit test otomatis validasi & round-trip Excel)
├── build.gradle & settings.gradle
└── SIMWarga-debug.apk (Berkas APK siap pakai)
```

---

## 🚀 Cara Instalasi & Menjalankan

### 1. Install APK Langsung di HP Android
1. Unduh atau salin berkas [`SIMWarga-debug.apk`](SIMWarga-debug.apk) ke HP Android Anda.
2. Buka berkas APK melalui File Manager HP untuk menginstall aplikasi.
3. Saat pertama kali dibuka, buat PIN 4-6 digit untuk mengamankan aplikasi.
4. Buka menu **"Excel & Data"** dan pilih **"Pilih Berkas Excel untuk Diimpor"** untuk memasukkan data warga lingkungan Anda.

### 2. Kompilasi dari Source Code
Pastikan Anda telah memiliki JDK 17 dan Android SDK (API 34):
```bash
# Menjalankan seluruh pengujian unit otomatis
./gradlew test

# Mengompilasi berkas APK Debug
./gradlew assembleDebug
```
Berkas APK hasil build akan berada di `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🧪 Pengujian Otomatis (Unit Testing)

Proyek ini dilengkapi serangkaian automated unit test yang memastikan integritas logika:
- **`ValidationAndHelperTest`**: Memverifikasi validasi 16 digit NIK/KK, format PIN, dan parser tanggal.
- **`ExcelRoundTripTest`**: Memverifikasi proses pembuatan berkas spreadsheet `.xlsx` dan pembacaan ulangnya tanpa ada data yang hilang.
- **`UserExcelFormatTest`**: Memverifikasi langsung parsing berkas riil RT 03 RW 07 (93 KK, 300+ warga) dan kompatibilitas format bolak-balik.

---

## 📄 Lisensi

Dikembangkan untuk kemaslahatan administrasi rukun warga dan rukun tetangga (RT/RW) di Indonesia. Bebas digunakan dan dikembangkan lebih lanjut.
