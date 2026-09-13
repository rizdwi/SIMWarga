package com.simwarga

import com.simwarga.util.DateHelper
import com.simwarga.util.ExcelHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExcelRoundTripTest {

    @Test
    fun testXlsxRoundTrip() {
        val originalRows = listOf(
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Melati No. 10",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201011508900001",
                "NAMA_LENGKAP" to "Ahmad Fauzi",
                "JENIS_KELAMIN" to "L",
                "TEMPAT_LAHIR" to "Depok",
                "TANGGAL_LAHIR" to "15/08/1990",
                "STATUS_HUBUNGAN" to "Kepala Keluarga",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "Sarjana (S1 / D4)",
                "PEKERJAAN" to "Pegawai Swasta",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            ),
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Melati No. 10",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201012005920002",
                "NAMA_LENGKAP" to "Dewi Lestari",
                "JENIS_KELAMIN" to "P",
                "TEMPAT_LAHIR" to "Bogor",
                "TANGGAL_LAHIR" to "20/05/1992",
                "STATUS_HUBUNGAN" to "Istri",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "Diploma (D1 - D3)",
                "PEKERJAAN" to "Ibu Rumah Tangga",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            )
        )

        // 1. Tulis ke OpenXML XLSX stream
        val bos = ByteArrayOutputStream()
        val meta = com.simwarga.util.WilayahMetadata(
            desa = "Sukamaju",
            kecamatan = "Cilodong",
            kabupaten = "Kota Depok",
            alamat = "Jl. Melati No. 10",
            rt = "001",
            rw = "002"
        )
        ExcelHelper.writeXlsx(bos, originalRows, meta)
        val xlsxBytes = bos.toByteArray()

        // 2. Baca kembali dari XLSX stream
        val parsedRows = ExcelHelper.readSpreadsheet(ByteArrayInputStream(xlsxBytes))

        // 3. Verifikasi
        assertEquals(2, parsedRows.size)

        for (i in originalRows.indices) {
            val original = originalRows[i]
            val parsed = parsedRows[i]
            for (col in ExcelHelper.COLUMNS) {
                assertEquals(
                    "Column $col at row $i must match",
                    original[col],
                    parsed[col]
                )
            }
        }
    }

    @Test
    fun testCsvParsingWithSemicolon() {
        val csvContent = """
            NO_KK;ALAMAT;RT;RW;KELURAHAN;KECAMATAN;KABUPATEN;NIK;NAMA_LENGKAP;JENIS_KELAMIN;TEMPAT_LAHIR;TANGGAL_LAHIR;STATUS_HUBUNGAN;AGAMA;PENDIDIKAN;PEKERJAAN;STATUS_PERKAWINAN;KEWARGANEGARAAN
            3201010000000001;Jl. Anggrek;003;004;Mekarjaya;Sukmajaya;Depok;3201011111110001;Budi;L;Jakarta;10/10/1985;Kepala Keluarga;Islam;SMA;Wiraswasta;Kawin;WNI
        """.trimIndent()

        val parsed = ExcelHelper.readSpreadsheet(ByteArrayInputStream(csvContent.toByteArray()))
        assertEquals(1, parsed.size)
        assertEquals("3201010000000001", parsed[0]["NO_KK"])
        assertEquals("3201011111110001", parsed[0]["NIK"])
        assertEquals("Budi", parsed[0]["NAMA_LENGKAP"])
        assertEquals("Sukmajaya", parsed[0]["KECAMATAN"])
    }

    @Test
    fun testDateParsingFlexibility() {
        val date1 = DateHelper.parseDate("15/08/1990")
        assertNotNull(date1)

        val date2 = DateHelper.parseDate("1990-08-15")
        assertNotNull(date2)
        assertEquals(date1, date2)

        val date3 = DateHelper.parseDate("15-08-1990")
        assertNotNull(date3)
        assertEquals(date1, date3)
    }
}
