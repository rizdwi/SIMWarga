package com.simwarga.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga

@Database(entities = [Keluarga::class, Anggota::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keluargaDao(): KeluargaDao
    abstract fun anggotaDao(): AnggotaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simwarga.db"
                ).build().also { INSTANCE = it }
            }
    }
}
