package app.podiumpodcasts.podium.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.FileProvider
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun unixSecondsToIso8601(seconds: Long): String? {
    val instant = Instant.ofEpochSecond(seconds)
    val utcDateTime = instant.atZone(ZoneId.of("UTC")).toLocalDateTime()

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return utcDateTime.format(formatter)
}

fun getFriendlyDeviceName(context: Context): String {
    val userDefined =
        Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    if(!userDefined.isNullOrBlank()) return userDefined

    val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val model = android.os.Build.MODEL

    return when(model.startsWith(manufacturer)) {
        true -> model
        false -> "$manufacturer $model"
    }
}

fun getCountryCode(
    context: Context?
): String {
    context?.let {
        try {
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkCountryCode = manager.networkCountryIso
            if(networkCountryCode.isNotBlank()) return networkCountryCode.uppercase()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    try {
        return Locale.getDefault().isO3Country.uppercase()
    } catch(e: Exception) {
        e.printStackTrace()
        return "US"
    }
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(toByteArray(Charsets.UTF_8))
    return hashBytes.fold("") { str, byte -> str + "%02x".format(byte) }
}

fun shareFile(
    context: Context,
    file: File,
    mimeType: String,
    title: String
) {
    val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            title
        )
    )
}