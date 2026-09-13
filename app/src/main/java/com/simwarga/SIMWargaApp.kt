package com.simwarga

import android.app.Application
import com.simwarga.data.db.AppDatabase
import com.simwarga.data.repository.WargaRepository

class SIMWargaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: WargaRepository by lazy { WargaRepository(database) }
}
