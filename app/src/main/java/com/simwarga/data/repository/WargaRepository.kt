package com.simwarga.data.repository

import com.simwarga.data.db.AppDatabase
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga
import com.simwarga.data.model.KeluargaWithAnggota
import com.simwarga.util.DateHelper
import com.simwarga.util.ExcelHelper
import com.simwarga.util.ValidationHelper
import com.simwarga.util.WilayahMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

data class ExcelImportResult(
    val totalRowsProcessed: Int,
    val kkAddedOrUpdated: Int,
    val anggotaAddedOrUpdated: Int,
    val skippedRows: List<String> = emptyList()
)

class WargaRepository(private val db: AppDatabase) {
    private val keluargaDao = db.keluargaDao()
    private val anggotaDao = db.anggotaDao()

    fun getAllKeluarga(): Flow<List<KeluargaWithAnggota>> =
        keluargaDao.getAllWithAnggota().flowOn(Dispatchers.IO)

    fun getKeluargaById(id: Int): Flow<KeluargaWithAnggota?> =
        keluargaDao.getByIdWithAnggota(id).flowOn(Dispatchers.IO)

    suspend fun getKeluargaDirect(id: Int): Keluarga? = withContext(Dispatchers.IO) {
        keluargaDao.getById(id)
    }

    fun search(query: String): Flow<List<KeluargaWithAnggota>> =
        keluargaDao.search(query.trim()).flowOn(Dispatchers.IO)

    fun countKk(): Flow<Int> = keluargaDao.countKk().flowOn(Dispatchers.IO)
    fun countJiwa(): Flow<Int> = anggotaDao.countJiwa().flowOn(Dispatchers.IO)
    fun countLaki(): Flow<Int> = anggotaDao.countLaki().flowOn(Dispatchers.IO)
    fun countPerempuan(): Flow<Int> = anggotaDao.countPerempuan().flowOn(Dispatchers.IO)

    suspend fun getByNoKk(noKk: String): Keluarga? = withContext(Dispatchers.IO) {
        keluargaDao.getByNoKk(noKk.trim())
    }

    suspend fun getByNik(nik: String): Anggota? = withContext(Dispatchers.IO) {
        anggotaDao.getByNik(nik.trim())
    }

    suspend fun getAnggotaById(id: Int): Anggota? = withContext(Dispatchers.IO) {
        anggotaDao.getById(id)
    }

    suspend fun insertKeluarga(keluarga: Keluarga): Long = withContext(Dispatchers.IO) {
        keluargaDao.insert(keluarga)
    }

    suspend fun updateKeluarga(keluarga: Keluarga) = withContext(Dispatchers.IO) {
        keluargaDao.update(keluarga)
    }

    suspend fun deleteKeluarga(keluarga: Keluarga) = withContext(Dispatchers.IO) {
        keluargaDao.delete(keluarga)
    }

    suspend fun insertAnggota(anggota: Anggota): Long = withContext(Dispatchers.IO) {
        anggotaDao.insert(anggota)
    }

    suspend fun updateAnggota(anggota: Anggota) = withContext(Dispatchers.IO) {
        anggotaDao.update(anggota)
    }

    suspend fun deleteAnggota(anggota: Anggota) = withContext(Dispatchers.IO) {
        anggotaDao.delete(anggota)
    }

    /**
     * Ekspor seluruh data KK & Anggota ke berkas spreadsheet Excel (.xlsx)
     * Format kolom persis sama dengan format impor.
     */
    suspend fun exportToExcel(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val allKeluarga = keluargaDao.getAllWithAnggota().first()
        val rows = mutableListOf<Map<String, String>>()

        for (item in allKeluarga) {
            val k = item.keluarga
            if (item.anggotaList.isEmpty()) {
                // KK tanpa anggota: tulis 1 baris berisi info KK
                rows.add(
                    mapOf(
                        "NO_KK" to k.noKk,
                        "ALAMAT" to k.alamat,
                        "RT" to k.rt,
                        "RW" to k.rw,
                        "KELURAHAN" to k.kelurahan,
                        "KECAMATAN" to k.kecamatan,
                        "KABUPATEN" to k.kabupaten,
                        "NIK" to "",
                        "NAMA_LENGKAP" to "",
                        "JENIS_KELAMIN" to "",
                        "TEMPAT_LAHIR" to "",
                        "TANGGAL_LAHIR" to "",
                        "STATUS_HUBUNGAN" to "",
                        "AGAMA" to "",
                        "PENDIDIKAN" to "",
                        "PEKERJAAN" to "",
                        "STATUS_PERKAWINAN" to "",
                        "KEWARGANEGARAAN" to ""
                    )
                )
            } else {
                // Setiap anggota keluarga ditulis sebagai 1 baris
                for (a in item.anggotaList) {
                    rows.add(
                        mapOf(
                            "NO_KK" to k.noKk,
                            "ALAMAT" to k.alamat,
                            "RT" to k.rt,
                            "RW" to k.rw,
                            "KELURAHAN" to k.kelurahan,
                            "KECAMATAN" to k.kecamatan,
                            "KABUPATEN" to k.kabupaten,
                            "NIK" to a.nik,
                            "NAMA_LENGKAP" to a.namaLengkap,
                            "JENIS_KELAMIN" to a.jenisKelamin,
                            "TEMPAT_LAHIR" to a.tempatLahir,
                            "TANGGAL_LAHIR" to DateHelper.formatShort(a.tanggalLahir),
                            "STATUS_HUBUNGAN" to a.statusHubungan,
                            "AGAMA" to a.agama,
                            "PENDIDIKAN" to a.pendidikan,
                            "PEKERJAAN" to a.pekerjaan,
                            "STATUS_PERKAWINAN" to a.statusPerkawinan,
                            "KEWARGANEGARAAN" to a.kewarganegaraan
                        )
                    )
                }
            }
        }

        val firstK = allKeluarga.firstOrNull()?.keluarga
        val meta = if (firstK != null) {
            WilayahMetadata(
                desa = firstK.kelurahan.ifBlank { "Balonggandu" },
                kecamatan = firstK.kecamatan.ifBlank { "Jatisari" },
                kabupaten = firstK.kabupaten.ifBlank { "Karawang" },
                alamat = firstK.alamat.ifBlank { "Perum Bumi Cikampek Baru" },
                rt = firstK.rt.ifBlank { "03" },
                rw = firstK.rw.ifBlank { "07" },
                tahun = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
            )
        } else WilayahMetadata()

        ExcelHelper.writeXlsx(outputStream, rows, meta)
    }

    /**
     * Mengunduh berkas template contoh Excel (.xlsx) sesuai format acuan RT 03 RW 07
     */
    suspend fun exportTemplate(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        ExcelHelper.writeXlsx(outputStream, ExcelHelper.createSampleTemplateRows(), WilayahMetadata())
    }

    /**
     * Mengimpor data dari berkas Excel (.xlsx) atau CSV
     * Otomatis mengelompokkan baris berdasarkan No. KK
     */
    suspend fun importFromExcel(inputStream: InputStream): ExcelImportResult = withContext(Dispatchers.IO) {
        val rawRows = ExcelHelper.readSpreadsheet(inputStream)
        if (rawRows.isEmpty()) {
            return@withContext ExcelImportResult(0, 0, 0, listOf("Berkas kosong atau format tidak dikenali."))
        }

        var kkCount = 0
        var anggotaCount = 0
        val skipped = mutableListOf<String>()

        // 1. Kelompokkan baris berdasarkan No. KK
        val groupedByKk = rawRows.groupBy { it["NO_KK"]?.trim().orEmpty() }

        db.runInTransaction {
            kotlinx.coroutines.runBlocking {
                for ((noKk, rows) in groupedByKk) {
                    if (noKk.isBlank()) {
                        skipped.add("Dilewati ${rows.size} baris: Nomor KK kosong.")
                        continue
                    }
                    if (!ValidationHelper.isValidNoKk(noKk)) {
                        skipped.add("Dilewati No. KK '$noKk': Format nomor KK harus 16 digit.")
                        continue
                    }

                    // Ambil info alamat dari baris pertama keluarga ini
                    val firstRow = rows.first()
                    var existingKeluarga = keluargaDao.getByNoKk(noKk)
                    val targetKkId: Int

                    if (existingKeluarga != null) {
                        // Perbarui data alamat KK jika ada perubahan di Excel
                        val updated = existingKeluarga.copy(
                            alamat = firstRow["ALAMAT"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.alamat,
                            rt = firstRow["RT"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.rt,
                            rw = firstRow["RW"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.rw,
                            kelurahan = firstRow["KELURAHAN"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.kelurahan,
                            kecamatan = firstRow["KECAMATAN"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.kecamatan,
                            kabupaten = firstRow["KABUPATEN"]?.takeIf { it.isNotBlank() } ?: existingKeluarga.kabupaten,
                            updatedAt = System.currentTimeMillis()
                        )
                        keluargaDao.update(updated)
                        targetKkId = existingKeluarga.id
                    } else {
                        // Buat entri KK baru
                        val newKeluarga = Keluarga(
                            noKk = noKk,
                            alamat = firstRow["ALAMAT"].orEmpty(),
                            rt = firstRow["RT"].orEmpty(),
                            rw = firstRow["RW"].orEmpty(),
                            kelurahan = firstRow["KELURAHAN"].orEmpty(),
                            kecamatan = firstRow["KECAMATAN"].orEmpty(),
                            kabupaten = firstRow["KABUPATEN"].orEmpty()
                        )
                        targetKkId = keluargaDao.insert(newKeluarga).toInt()
                    }
                    kkCount++

                    // 2. Simpan setiap anggota keluarga
                    for (row in rows) {
                        val nik = row["NIK"]?.trim().orEmpty()
                        val nama = row["NAMA_LENGKAP"]?.trim().orEmpty()

                        // Baris tanpa NIK atau nama dilewati (misal baris KK kosong)
                        if (nik.isBlank() && nama.isBlank()) continue

                        if (!ValidationHelper.isValidNik(nik)) {
                            skipped.add("Dilewati NIK '$nik' ($nama): NIK harus tepat 16 digit.")
                            continue
                        }

                        val existingAnggota = anggotaDao.getByNik(nik)
                        val tglLahirMillis = DateHelper.parseDate(row["TANGGAL_LAHIR"].orEmpty()) ?: 0L
                        val jk = when (row["JENIS_KELAMIN"]?.trim()?.uppercase()) {
                            "P", "PEREMPUAN", "WANITA", "F" -> "P"
                            else -> "L"
                        }

                        if (existingAnggota != null) {
                            // Update data anggota
                            val updatedAnggota = existingAnggota.copy(
                                kkId = targetKkId,
                                namaLengkap = if (nama.isNotBlank()) nama else existingAnggota.namaLengkap,
                                jenisKelamin = jk,
                                tempatLahir = row["TEMPAT_LAHIR"]?.takeIf { it.isNotBlank() } ?: existingAnggota.tempatLahir,
                                tanggalLahir = if (tglLahirMillis > 0L) tglLahirMillis else existingAnggota.tanggalLahir,
                                statusHubungan = row["STATUS_HUBUNGAN"]?.takeIf { it.isNotBlank() } ?: existingAnggota.statusHubungan,
                                agama = row["AGAMA"]?.takeIf { it.isNotBlank() } ?: existingAnggota.agama,
                                pendidikan = row["PENDIDIKAN"]?.takeIf { it.isNotBlank() } ?: existingAnggota.pendidikan,
                                pekerjaan = row["PEKERJAAN"]?.takeIf { it.isNotBlank() } ?: existingAnggota.pekerjaan,
                                statusPerkawinan = row["STATUS_PERKAWINAN"]?.takeIf { it.isNotBlank() } ?: existingAnggota.statusPerkawinan,
                                kewarganegaraan = row["KEWARGANEGARAAN"]?.takeIf { it.isNotBlank() } ?: existingAnggota.kewarganegaraan,
                                updatedAt = System.currentTimeMillis()
                            )
                            anggotaDao.update(updatedAnggota)
                        } else {
                            // Tambah anggota baru
                            val newAnggota = Anggota(
                                kkId = targetKkId,
                                nik = nik,
                                namaLengkap = if (nama.isNotBlank()) nama else "Warga Baru",
                                jenisKelamin = jk,
                                tempatLahir = row["TEMPAT_LAHIR"].orEmpty(),
                                tanggalLahir = tglLahirMillis,
                                statusHubungan = row["STATUS_HUBUNGAN"]?.ifBlank { "Lainnya" } ?: "Lainnya",
                                agama = row["AGAMA"].orEmpty(),
                                pendidikan = row["PENDIDIKAN"].orEmpty(),
                                pekerjaan = row["PEKERJAAN"].orEmpty(),
                                statusPerkawinan = row["STATUS_PERKAWINAN"].orEmpty(),
                                kewarganegaraan = row["KEWARGANEGARAAN"]?.ifBlank { "WNI" } ?: "WNI"
                            )
                            anggotaDao.insert(newAnggota)
                        }
                        anggotaCount++
                    }
                }
            }
        }

        ExcelImportResult(
            totalRowsProcessed = rawRows.size,
            kkAddedOrUpdated = kkCount,
            anggotaAddedOrUpdated = anggotaCount,
            skippedRows = skipped
        )
    }
}
