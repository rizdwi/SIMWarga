package com.simwarga.util

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest

object PinHelper {
    private const val PREF_NAME = "simwarga_secure_prefs"
    private const val KEY_PIN_HASH = "key_pin_hash"
    private const val KEY_PIN_IS_SET = "key_pin_is_set"

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun isPinSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PIN_IS_SET, false)
    }

    fun setPin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PIN_HASH, sha256(pin.trim()))
            putBoolean(KEY_PIN_IS_SET, true)
        }
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == sha256(pin.trim())
    }
}
