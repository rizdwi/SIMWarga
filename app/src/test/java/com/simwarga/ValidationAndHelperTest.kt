package com.simwarga

import com.google.gson.Gson
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga
import com.simwarga.util.BackupPayload
import com.simwarga.util.DateHelper
import com.simwarga.util.ValidationHelper
import org.junit.Assert.*
import org.junit.Test

class ValidationAndHelperTest {

    @Test
    fun testValidNik() {
        assertTrue(ValidationHelper.isValidNik("3201010101010001"))
        assertFalse(ValidationHelper.isValidNik("123")) // Too short
        assertFalse(ValidationHelper.isValidNik("32010101010100019")) // Too long
        assertFalse(ValidationHelper.isValidNik("320101010101000A")) // Non-digit
    }

    @Test
    fun testValidNoKk() {
        assertTrue(ValidationHelper.isValidNoKk("3201010101010002"))
        assertFalse(ValidationHelper.isValidNoKk("3201010101"))
        assertFalse(ValidationHelper.isValidNoKk("320101010101000200"))
        assertFalse(ValidationHelper.isValidNoKk("320101010101000X"))
    }

    @Test
    fun testValidPin() {
        assertTrue(ValidationHelper.isValidPin("1234"))
        assertTrue(ValidationHelper.isValidPin("123456"))
        assertFalse(ValidationHelper.isValidPin("123"))
        assertFalse(ValidationHelper.isValidPin("1234567"))
        assertFalse(ValidationHelper.isValidPin("123a"))
    }

    @Test
    fun testDateHelper() {
        val parsed = DateHelper.parseShort("13/09/2026")
        assertNotNull(parsed)
        val formatted = DateHelper.formatShort(parsed!!)
        assertEquals("13/09/2026", formatted)
    }

    @Test
    fun testBackupPayloadSerialization() {
        val keluarga = Keluarga(
            id = 1,
            noKk = "3201010101010001",
            alamat = "Jl. Melati No. 10",
            rt = "001",
            rw = "002",
            kelurahan = "Sukamaju",
            kecamatan = "Cilodong",
            kabupaten = "Depok"
        )
        val anggota = Anggota(
            id = 1,
            kkId = 1,
            nik = "3201010101010002",
            namaLengkap = "Budi Santoso",
            jenisKelamin = "L",
            tempatLahir = "Depok",
            tanggalLahir = 631152000000L,
            statusHubungan = "Kepala Keluarga",
            agama = "Islam",
            pendidikan = "Sarjana (S1 / D4)",
            pekerjaan = "Karyawan Swasta",
            statusPerkawinan = "Kawin"
        )

        val payload = BackupPayload(
            keluarga = listOf(keluarga),
            anggota = listOf(anggota)
        )

        val gson = Gson()
        val json = gson.toJson(payload)
        val restored = gson.fromJson(json, BackupPayload::class.java)

        assertEquals("SIMWarga", restored.app)
        assertEquals(1, restored.keluarga.size)
        assertEquals("3201010101010001", restored.keluarga[0].noKk)
        assertEquals(1, restored.anggota.size)
        assertEquals("Budi Santoso", restored.anggota[0].namaLengkap)
    }
}
