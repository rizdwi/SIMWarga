package com.simwarga.data.model

import androidx.room.*

@Entity(tableName = "keluarga", indices = [Index(value = ["no_kk"], unique = true)])
data class Keluarga(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "no_kk") val noKk: String,
    val alamat: String,
    val rt: String,
    val rw: String,
    val kelurahan: String,
    val kecamatan: String,
    val kabupaten: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
