package com.simwarga

import com.simwarga.util.ExcelHelper
import com.simwarga.util.WilayahMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class UserExcelFormatTest {

    @Test
    fun testRtRwFormatRoundTrip() {
        val originalRows = listOf(
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Mawar No. 12",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201011508900001",
                "NAMA_LENGKAP" to "Budi Santoso",
                "JENIS_KELAMIN" to "L",
                "TEMPAT_LAHIR" to "Depok",
                "TANGGAL_LAHIR" to "15/08/1990",
                "STATUS_HUBUNGAN" to "Kepala Keluarga",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "SLTA",
                "PEKERJAAN" to "Karyawan Swasta",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            ),
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Mawar No. 12",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201014605920002",
                "NAMA_LENGKAP" to "Siti Rahmawati",
                "JENIS_KELAMIN" to "P",
                "TEMPAT_LAHIR" to "Bogor",
                "TANGGAL_LAHIR" to "20/05/1992",
                "STATUS_HUBUNGAN" to "Istri",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "SLTA",
                "PEKERJAAN" to "Ibu Rumah Tangga",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            )
        )

        // 1. Tulis ke OpenXML XLSX stream dengan format acuan Buku Induk RT/RW
        val bos = ByteArrayOutputStream()
        val meta = WilayahMetadata(
            desa = "Sukamaju",
            kecamatan = "Cilodong",
            kabupaten = "Kota Depok",
            alamat = "Jl. Mawar No. 12",
            rt = "001",
            rw = "002",
            tahun = "2024"
        )
        ExcelHelper.writeXlsx(bos, originalRows, meta)
        val xlsxBytes = bos.toByteArray()

        // 2. Baca kembali dari XLSX stream
        val parsedRows = ExcelHelper.readSpreadsheet(ByteArrayInputStream(xlsxBytes))

        // 3. Verifikasi round-trip
        assertEquals(2, parsedRows.size)

        assertEquals("Budi Santoso", parsedRows[0]["NAMA_LENGKAP"])
        assertEquals("Kepala Keluarga", parsedRows[0]["STATUS_HUBUNGAN"])
        assertEquals("3201012304050001", parsedRows[0]["NO_KK"])
        assertEquals("3201011508900001", parsedRows[0]["NIK"])

        assertEquals("Siti Rahmawati", parsedRows[1]["NAMA_LENGKAP"])
        assertEquals("Istri", parsedRows[1]["STATUS_HUBUNGAN"])
        assertEquals("3201012304050001", parsedRows[1]["NO_KK"])
        assertEquals("3201014605920002", parsedRows[1]["NIK"])
    }

    @Test
    fun testLocalFileIfPresent() {
        val userFile = File("c:/Users/Rizz/Documents/Project1/DATA JIWA USIK WARGA RT 03  RW 07 TAHUN 2023 des23 (1).xlsx")
        if (userFile.exists()) {
            val inputStream = FileInputStream(userFile)
            val rows = ExcelHelper.readSpreadsheet(inputStream)
            assertFalse("Jika berkas lokal ada, baris tidak boleh kosong", rows.isEmpty())
            assertTrue("Memverifikasi pembacaan baris warga lokal", rows.size >= 100)
        }
    }
}
