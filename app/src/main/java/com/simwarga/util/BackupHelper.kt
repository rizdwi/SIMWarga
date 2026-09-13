package com.simwarga.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga

data class BackupPayload(
    val app: String = "SIMWarga",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val keluarga: List<Keluarga>,
    val anggota: List<Anggota>
)

object BackupHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportBackup(
        context: Context,
        destinationUri: Uri,
        keluargaList: List<Keluarga>,
        anggotaList: List<Anggota>
    ): Boolean {
        return try {
            val payload = BackupPayload(
                keluarga = keluargaList,
                anggota = anggotaList
            )
            val json = gson.toJson(payload)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importBackup(context: Context, sourceUri: Uri): BackupPayload? {
        return try {
            val json = context.contentResolver.openInputStream(sourceUri)?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText()
            } ?: return null

            val type = object : TypeToken<BackupPayload>() {}.type
            gson.fromJson<BackupPayload>(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
