package com.simwarga.data.db

import androidx.room.*
import com.simwarga.data.model.Anggota
import kotlinx.coroutines.flow.Flow

@Dao
interface AnggotaDao {
    @Query("SELECT * FROM anggota WHERE kk_id = :kkId ORDER BY CASE status_hubungan WHEN 'Kepala Keluarga' THEN 1 WHEN 'Istri' THEN 2 WHEN 'Anak' THEN 3 ELSE 4 END ASC, tanggal_lahir ASC")
    fun getByKkId(kkId: Int): Flow<List<Anggota>>

    @Query("SELECT * FROM anggota WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Anggota?

    @Query("SELECT * FROM anggota WHERE nik = :nik LIMIT 1")
    suspend fun getByNik(nik: String): Anggota?

    @Query("SELECT COUNT(*) FROM anggota")
    fun countJiwa(): Flow<Int>

    @Query("SELECT COUNT(*) FROM anggota WHERE jenis_kelamin = 'L'")
    fun countLaki(): Flow<Int>

    @Query("SELECT COUNT(*) FROM anggota WHERE jenis_kelamin = 'P'")
    fun countPerempuan(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(anggota: Anggota): Long

    @Update
    suspend fun update(anggota: Anggota)

    @Delete
    suspend fun delete(anggota: Anggota)

    @Query("SELECT * FROM anggota")
    suspend fun getAllForBackup(): List<Anggota>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(anggotaList: List<Anggota>)

    @Query("DELETE FROM anggota")
    suspend fun deleteAll()
}
