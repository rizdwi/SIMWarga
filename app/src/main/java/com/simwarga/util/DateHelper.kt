package com.simwarga.util

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    private val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val shortFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

    private val parseFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")),
        SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")),
        SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")),
        SimpleDateFormat("d/M/yyyy", Locale("id", "ID"))
    )

    fun formatDisplay(epochMillis: Long): String {
        return if (epochMillis <= 0L) "-" else displayFormat.format(Date(epochMillis))
    }

    fun formatShort(epochMillis: Long): String {
        return if (epochMillis <= 0L) "" else shortFormat.format(Date(epochMillis))
    }

    fun parseDate(text: String): Long? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        // 1. Coba format standar
        for (format in parseFormats) {
            try {
                format.isLenient = false
                val parsed = format.parse(clean)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { }
        }

        // 2. Coba jika berupa nomor serial tanggal Excel (misal: 33100)
        val serialDays = clean.toDoubleOrNull()
        if (serialDays != null && serialDays in 1000.0..100000.0) {
            // Epoch untuk Excel serial 1 = 1899-12-31 (termasuk leap year bug 1900)
            val cal = GregorianCalendar(1899, Calendar.DECEMBER, 30)
            cal.add(Calendar.DAY_OF_YEAR, serialDays.toInt())
            return cal.timeInMillis
        }

        return null
    }

    fun parseShort(text: String): Long? = parseDate(text)
}
