package com.simwarga.util

object ValidationHelper {
    /**
     * Validasi format NIK: tepat 16 digit angka
     */
    fun isValidNik(nik: String): Boolean = nik.trim().matches(Regex("^[0-9]{16}$"))

    /**
     * Validasi format No. KK: tepat 16 digit angka
     */
    fun isValidNoKk(noKk: String): Boolean = noKk.trim().matches(Regex("^[0-9]{16}$"))

    /**
     * Validasi format PIN: 4 sampai 6 digit angka
     */
    fun isValidPin(pin: String): Boolean = pin.trim().matches(Regex("^[0-9]{4,6}$"))
}
