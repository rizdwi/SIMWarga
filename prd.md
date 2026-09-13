# PRD: Aplikasi Pendataan KK/KTP Tingkat RT/RW (Offline)

| | |
|---|---|
| **Nama Produk** | SIMWarga (contoh nama, bisa diganti) |
| **Platform** | Android (native, offline-first) |
| **Versi Dokumen** | 1.0 |
| **Tanggal** | 13 September 2026 |

---

## 1. Ringkasan Eksekutif
Aplikasi Android untuk pendataan Kartu Keluarga (KK) dan Kartu Tanda Penduduk (KTP) warga di tingkat RT/RW. Aplikasi berjalan **100% lokal di HP** tanpa koneksi internet, dan secara otomatis **mengelompokkan anggota keluarga berdasarkan No. KK** sehingga data tidak berantakan dan mudah ditelusuri per keluarga.

## 2. Latar Belakang & Masalah
- Pendataan warga di RT/RW umumnya masih manual (buku/Excel), rawan hilang, sulit dicari, dan sering terjadi duplikasi data.
- Data KK dan anggota keluarga sering tercampur tanpa struktur relasi yang jelas.
- Banyak wilayah tidak punya akses internet stabil, sehingga aplikasi berbasis cloud kurang cocok.

## 3. Tujuan
1. Mendigitalkan pendataan KK/KTP warga tanpa ketergantungan internet.
2. Mengelompokkan data anggota keluarga secara otomatis berdasarkan No. KK, bukan baris-baris terpisah yang berantakan.
3. Mempermudah pencarian, rekap, dan pelaporan data warga oleh pengurus RT/RW.

## 4. Target Pengguna
- Ketua RT / Ketua RW
- Sekretaris RT/RW
- Petugas pendataan warga (kader/relawan)

## 5. Ruang Lingkup
**In Scope:**
- Input, edit, hapus data KK dan anggota keluarga
- Pengelompokan otomatis berdasarkan No. KK
- Pencarian & filter data
- Backup/restore data secara lokal (file di penyimpanan HP)
- Statistik ringkas jumlah KK & jiwa

**Out of Scope (versi ini):**
- Sinkronisasi cloud / server pusat
- Multi-device real-time sync
- Integrasi dengan Dukcapil/API pemerintah
- Login online / akun berbasis internet

## 6. Kebutuhan Fungsional (Functional Requirements)

| ID | Fitur | Deskripsi |
|---|---|---|
| FR-01 | Tambah/Edit KK | Input data Kartu Keluarga: No. KK, alamat, RT/RW, kelurahan, kecamatan |
| FR-02 | Tambah/Edit Anggota | Input data individu (NIK, nama, dll) yang terhubung ke satu No. KK |
| FR-03 | Grouping Otomatis | Semua anggota dengan No. KK sama otomatis tergabung dalam satu entri/tabel KK (tidak perlu manual) |
| FR-04 | Tampilan Tabel Terstruktur | Daftar KK ditampilkan sebagai kartu/baris utama yang bisa di-expand untuk melihat semua anggotanya |
| FR-05 | Validasi Duplikat NIK | Sistem menolak/menandai jika NIK yang sama sudah terdaftar |
| FR-06 | Pencarian & Filter | Cari berdasarkan NIK, nama, No. KK, atau alamat; hasil tetap ditampilkan per grup KK |
| FR-07 | Hapus Data | Hapus anggota atau seluruh KK beserta anggotanya (dengan konfirmasi) |
| FR-08 | Dashboard Statistik | Total KK, total jiwa, rekap jenis kelamin/usia secara ringkas |
| FR-09 | Backup & Restore Lokal | Export seluruh data ke file (JSON/CSV/Excel) ke penyimpanan HP; import kembali saat ganti perangkat |
| FR-10 | Export Laporan | Cetak/export rekap data ke PDF atau Excel untuk keperluan RT/RW |
| FR-11 | Keamanan Akses | Kunci aplikasi dengan PIN/password lokal karena data bersifat sensitif |

## 7. Kebutuhan Non-Fungsional
- **Offline total**: tidak memerlukan izin internet sama sekali (permission `INTERNET` dihilangkan dari manifest).
- **Penyimpanan lokal**: menggunakan database SQLite (Room) di internal storage HP.
- **Performa**: tetap responsif untuk data hingga ribuan jiwa.
- **Kemudahan pakai**: UI sederhana, mengingat pengguna (pengurus RT/RW) belum tentu melek teknologi.
- **Keandalan data**: data tidak hilang saat aplikasi ditutup/HP restart.

## 8. Struktur Data (Skema Database)
Kunci dari fitur "tidak berantakan" adalah relasi **satu KK memiliki banyak anggota** (one-to-many).

**Tabel `keluarga` (KK)**
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | ID internal |
| no_kk | TEXT (UNIQUE) | Nomor KK, 16 digit |
| alamat | TEXT | |
| rt, rw | TEXT | |
| kelurahan, kecamatan, kabupaten | TEXT | |
| created_at, updated_at | DATETIME | |

**Tabel `anggota`**
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | ID internal |
| kk_id | INTEGER (FK → keluarga.id) | Penghubung ke satu KK |
| nik | TEXT (UNIQUE) | 16 digit, tidak boleh duplikat |
| nama_lengkap | TEXT | |
| jenis_kelamin | TEXT | L/P |
| tempat_lahir, tanggal_lahir | TEXT/DATE | |
| status_hubungan_keluarga | TEXT | Kepala Keluarga / Istri / Anak / dll |
| agama | TEXT | |
| pendidikan | TEXT | |
| pekerjaan | TEXT | |
| status_perkawinan | TEXT | |
| kewarganegaraan | TEXT | |
| created_at, updated_at | DATETIME | |

> Dengan struktur ini, saat menampilkan data, query cukup `SELECT * FROM anggota WHERE kk_id = ?` sehingga semua anggota dari satu KK otomatis tergabung rapi — tidak ada data yang tercecer di luar keluarganya.

## 9. Alur Pengguna Utama
1. Buka aplikasi → Dashboard (ringkasan statistik).
2. Tambah KK baru → isi No. KK & alamat.
3. Tambah anggota keluarga → pilih/isi No. KK terkait → sistem otomatis mengelompokkan ke KK tersebut & mengecek duplikat NIK.
4. Buka menu "Daftar KK" → tiap KK tampil sebagai satu baris/kartu → tap untuk expand melihat semua anggotanya.
5. Cari data via NIK/nama/No. KK → hasil tetap ditampilkan dalam konteks KK-nya.
6. Backup data secara berkala ke file lokal (mis. folder Documents di HP).

## 10. Daftar Layar (Screens)
1. Splash + Kunci PIN (opsional)
2. Dashboard (statistik ringkas)
3. Daftar KK (list + search bar)
4. Detail KK (info KK + tabel anggota di dalamnya)
5. Form Tambah/Edit KK
6. Form Tambah/Edit Anggota
7. Pencarian Global
8. Backup & Restore
9. Pengaturan

## 11. Rekomendasi Tech Stack
- **Bahasa/Platform**: Kotlin (Android native) — akses storage & performa lokal lebih stabil untuk kebutuhan ini.
- **Database**: Room (wrapper SQLite) — mendukung relasi one-to-many dengan mudah lewat `@Relation`.
- **Arsitektur**: MVVM (ViewModel + LiveData/Flow).
- **Export/Import**: Apache POI (Excel) atau format CSV/JSON native, disimpan via Storage Access Framework.
- **Alternatif cross-platform**: Flutter + `sqflite`, jika ke depan ingin dukungan iOS.

## 12. Keamanan & Privasi Data
- Data NIK/KK termasuk data pribadi sensitif → wajib:
  - Kunci akses aplikasi (PIN/password).
  - Opsi enkripsi database (misalnya SQLCipher) agar data tidak mudah dibaca jika HP hilang.
  - Tidak ada permission internet sama sekali di manifest, memastikan data tidak bisa keluar perangkat.
  - Pengingat backup rutin ke penyimpanan lokal/kartu SD.

## 13. Batasan (Constraints)
- Tidak ada sinkronisasi otomatis antar perangkat (semua backup/restore manual).
- Tanggung jawab menjaga file backup ada di pengguna (RT/RW).
- Tidak ada akun/login berbasis server.

## 14. Roadmap Pengembangan
**Fase 1 – MVP**
- CRUD KK & Anggota
- Grouping otomatis berdasarkan No. KK
- Pencarian dasar
- Backup/restore file lokal

**Fase 2**
- Dashboard statistik lebih lengkap (grafik usia, jenis kelamin, dll)
- Export laporan siap cetak (PDF/Excel)
- PIN + enkripsi database

**Fase 3 (opsional)**
- Dukungan multi-RT/RW dalam satu aplikasi (level kelurahan)
- Import massal dari file Excel/CSV data lama

## 15. Kriteria Keberhasilan
- 0% duplikasi NIK dalam database.
- Semua anggota keluarga tampil terkelompok benar di bawah No. KK yang sesuai, meski data mencapai ratusan/ribuan jiwa.
- Aplikasi berjalan tanpa memerlukan koneksi internet sama sekali.
- Proses input satu KK beserta anggotanya dapat diselesaikan dengan cepat dan tanpa error.

## 16. Risiko & Mitigasi
| Risiko | Mitigasi |
|---|---|
| Kehilangan data karena HP rusak/hilang | Fitur reminder backup rutin ke file lokal |
| Kesalahan input NIK | Validasi format 16 digit + pengecekan duplikat otomatis |
| Pengguna awam kesulitan memakai aplikasi | UI sederhana + tutorial singkat saat pertama pakai |

## 17. Lampiran: Field Standar KTP/KK
NIK, Nama Lengkap, Tempat/Tanggal Lahir, Jenis Kelamin, Alamat, RT/RW, Kelurahan/Desa, Kecamatan, Agama, Status Perkawinan, Pekerjaan, Kewarganegaraan, No. KK, Nama Kepala Keluarga, Status Hubungan dalam Keluarga.
