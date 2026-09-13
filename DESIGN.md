# DESIGN.md - SIMWarga

Design direction for SIMWarga (Aplikasi Pendataan KK/KTP RT/RW Offline).
Built following anti-slop rules (ENERGY 1 / RHYTHM 2 / MOTION 1).

## Identity & Purpose
- **Product:** SIMWarga (Sistem Informasi Manajemen Warga RT/RW).
- **Target Users:** Pengurus RT/RW, sekretaris, kader pendataan warga yang membutuhkan antarmuka yang jelas, mudah dipahami, dan tidak berbelit-belit.
- **Personality:** Terpercaya, fungsional, ramah pengguna, berorientasi layanan publik lokal.

## Palette (Dose-Capped)
- **Primary:** `#1565C0` (Deep Blue, warna standar layanan publik/pemerintahan yang formal dan tepercaya).
- **Primary Dark:** `#0D47A1`
- **Secondary / Accent:** `#2E7D32` (Forest Green, digunakan hemat sebagai penanda status berhasil, tombol simpan, atau konfirmasi).
- **Background:** `#F8F9FA` (Soft neutral grey, mengurangi silau saat penggunaan outdoor/pencatatan lapangan).
- **Surface:** `#FFFFFF` (Card dan container data).
- **On Background / Surface:** `#1A1A1A` (Teks kontras tinggi memenuhi WCAG AA > 7:1).
- **Secondary Text:** `#5F6368` (Keterangan/subteks pendukung).
- **Error:** `#C62828` (Penanda validasi/error).

## Typography
- **Font Family:** Roboto (Android standard system font).
- **Rationale:** Memastikan keterbacaan optimal di semua perangkat Android tanpa overhead custom font, konsisten dengan kontrol aksesibilitas ukuran font sistem.

## Dials
- **ENERGY:** 1 (Calm, fokus pada akurasi input data).
- **RHYTHM:** 2 (Terstruktur dengan pembeda visual antar kartu KK dan tabel anggota).
- **MOTION:** 1 (Transisi navigasi standar platform, tanpa animasi berlebihan yang mengganggu).

## Anti-Slop Rules Applied
- Tanpa gradien ungu-biru generic.
- Tanpa glassmorphism atau efek blur bertumpuk.
- Tanpa badge atau kapsul artifisial ("AI Powered").
- Semua elemen interaktif memiliki fungsi nyata (CRUD, filter, backup, PIN).
- Penanganan state lengkap (Empty, Loading, Error).
