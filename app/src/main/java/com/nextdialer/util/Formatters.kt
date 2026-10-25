package com.nextdialer.util

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatPhone(context: Context, number: String, enabled: Boolean): String {
        if (!enabled || number.isBlank()) return number
        val iso = runCatching {
            context.getSystemService(TelephonyManager::class.java)?.networkCountryIso
        }.getOrNull()?.takeIf { it.length == 2 } ?: Locale.getDefault().country
        return runCatching {
            PhoneNumberUtils.formatNumber(number, iso) ?: number
        }.getOrDefault(number)
    }

    fun callTimeLabel(
        context: Context,
        date: Long,
        relative: Boolean,
        displayTime: Boolean,
    ): String {
        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(date).atZone(zone)
        val day = zoned.toLocalDate()
        val today = LocalDate.now(zone)
        val time = android.text.format.DateFormat.getTimeFormat(context).format(Date(date))
        val dateText = dateText(day, today)

        if (!relative) {
            return if (displayTime) "$dateText · $time" else dateText
        }

        val dayDiff = ChronoUnit.DAYS.between(day, today)
        return when {
            dayDiff == 0L -> {
                val minutes = ((System.currentTimeMillis() - date) / 60_000L).coerceAtLeast(0)
                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "$minutes min. ago"
                    else -> time
                }
            }
            dayDiff == 1L -> if (displayTime) "Yesterday · $time" else "Yesterday"
            dayDiff in 2..6 -> if (displayTime) "$dayDiff days ago · $time" else "$dayDiff days ago"
            else -> if (displayTime) "$dateText · $time" else dateText
        }
    }

    fun dayLabel(date: Long): String {
        val zone = ZoneId.systemDefault()
        val day = Instant.ofEpochMilli(date).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when {
            day == today -> "Today"
            day == today.minusDays(1) -> "Yesterday"
            day.isAfter(today.minusDays(7)) -> "Earlier"
            else -> dateText(day, today)
        }
    }

    private fun dateText(day: LocalDate, today: LocalDate): String {
        val pattern = if (day.year == today.year) "MMM d" else "MMM d, yyyy"
        return day.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return ""
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }
}
