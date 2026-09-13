package com.simwarga.data.model

import androidx.room.*

@Entity(
    tableName = "anggota",
    foreignKeys = [ForeignKey(
        entity = Keluarga::class,
        parentColumns = ["id"],
        childColumns = ["kk_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["nik"], unique = true), Index(value = ["kk_id"])]
)
data class Anggota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "kk_id") val kkId: Int,
    val nik: String,
    @ColumnInfo(name = "nama_lengkap") val namaLengkap: String,
    @ColumnInfo(name = "jenis_kelamin") val jenisKelamin: String, // "L" or "P"
    @ColumnInfo(name = "tempat_lahir") val tempatLahir: String,
    @ColumnInfo(name = "tanggal_lahir") val tanggalLahir: Long,
    @ColumnInfo(name = "status_hubungan") val statusHubungan: String,
    val agama: String,
    val pendidikan: String,
    val pekerjaan: String,
    @ColumnInfo(name = "status_perkawinan") val statusPerkawinan: String,
    val kewarganegaraan: String = "WNI",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
