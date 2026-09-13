package com.simwarga.data.db

import androidx.room.*
import com.simwarga.data.model.Keluarga
import com.simwarga.data.model.KeluargaWithAnggota
import kotlinx.coroutines.flow.Flow

@Dao
interface KeluargaDao {
    @Transaction
    @Query("SELECT * FROM keluarga ORDER BY no_kk ASC")
    fun getAllWithAnggota(): Flow<List<KeluargaWithAnggota>>

    @Transaction
    @Query("SELECT * FROM keluarga WHERE id = :id LIMIT 1")
    fun getByIdWithAnggota(id: Int): Flow<KeluargaWithAnggota?>

    @Transaction
    @Query("""
        SELECT DISTINCT k.* FROM keluarga k
        LEFT JOIN anggota a ON a.kk_id = k.id
        WHERE k.no_kk LIKE '%' || :query || '%'
           OR k.alamat LIKE '%' || :query || '%'
           OR a.nik LIKE '%' || :query || '%'
           OR a.nama_lengkap LIKE '%' || :query || '%'
        ORDER BY k.no_kk ASC
    """)
    fun search(query: String): Flow<List<KeluargaWithAnggota>>

    @Query("SELECT COUNT(*) FROM keluarga")
    fun countKk(): Flow<Int>

    @Query("SELECT * FROM keluarga WHERE no_kk = :noKk LIMIT 1")
    suspend fun getByNoKk(noKk: String): Keluarga?

    @Query("SELECT * FROM keluarga WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Keluarga?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(keluarga: Keluarga): Long

    @Update
    suspend fun update(keluarga: Keluarga)

    @Delete
    suspend fun delete(keluarga: Keluarga)

    @Query("SELECT * FROM keluarga")
    suspend fun getAllForBackup(): List<Keluarga>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keluargaList: List<Keluarga>)

    @Query("DELETE FROM keluarga")
    suspend fun deleteAll()
}
