package app.podiumpodcasts.podium.ui

import android.content.Context
import android.content.ContextWrapper
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.isDigitsOnly
import app.podiumpodcasts.podium.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
This file contains misc UI util functions
 */

fun Context.findActivity(): ComponentActivity? = when(this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun parsePubDate(pubDate: String): Long {
    try {
        val dateFormatterRssPubDate: DateFormat =
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

        var pubDateInstant = dateFormatterRssPubDate.parse(pubDate)?.toInstant()
        if(pubDateInstant == null) pubDateInstant = Instant.parse(pubDate)

        return pubDateInstant.epochSecond
    } catch(e: Exception) {
        e.printStackTrace()
    }

    return -1
}

/**
 * Returns duration in seconds
 */
fun parseItunesDuration(duration: String): Int {
    if(duration.isEmpty())
        return 0

    if(duration.isDigitsOnly())
        return duration.toInt()

    val parts = duration.split(":")
    if(parts.size == 2) return parts[0].toInt() * 60 + parts[1].toInt()
    return parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[0].toInt()
}

/* Durations */
fun formatPlayerTime(
    time: Long,
    duration: Long = 0L
): String {
    val totalSeconds = time / 1000

    val hours = (totalSeconds / 3600).toString().padStart(2, '0')
    val minutes = ((totalSeconds % 3600) / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')

    var result = "${minutes}:${seconds}"
    if(duration > (3600 * 1000) || totalSeconds > 3600) result = "${hours}:${result}"
    return result
}

fun formatEpisodePlayTime(
    context: Context,
    duration: Int,
    state: Int = 0
): String {
    val totalMin = (duration - state) / 60

    val hours = totalMin / 60
    val minutes = totalMin % 60

    val text = when(hours > 0) {
        true -> context.getString(R.string.format_play_time_hours_and_minutes, hours, minutes)
        false -> context.getString(R.string.format_play_time_minutes, minutes.coerceAtLeast(1))
    }

    return when(state > 0) {
        true -> context.getString(R.string.format_play_time_left, text)
        false -> text
    }
}

fun formatPubDate(
    context: Context,
    pubDate: Long
): String {
    try {
        val pubDateInstant = Instant.ofEpochSecond(pubDate)

        val duration = Duration.between(
            pubDateInstant,
            Instant.now()
        ).abs()

        return when {
            duration.toDays() > 30 -> {
                LocalDateTime.ofInstant(pubDateInstant, ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault()))
            }

            duration.toDays() == 1L -> context.getString(R.string.format_date_yesterday)
            duration.toDays() > 0 -> context.getString(
                R.string.format_date_days_ago,
                duration.toDays()
            )

            duration.toHours() > 0 -> context.getString(
                R.string.format_date_hours_ago,
                duration.toHours()
            )

            duration.toMinutes() > 0 -> context.getString(
                R.string.format_date_minutes_ago,
                duration.toMinutes()
            )

            else -> context.getString(R.string.format_date_just_now)
        }
    } catch(e: Exception) {
        e.printStackTrace()
    }

    return "Could not parse pubDate"
}

@Composable
fun formatFileSize(
    sizeInBytes: Long
): String {
    val context = LocalContext.current
    var string by remember { mutableStateOf("") }

    LaunchedEffect(sizeInBytes) {
        string = Formatter.formatFileSize(context, sizeInBytes)
    }

    return string
}