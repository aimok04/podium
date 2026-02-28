package app.podiumpodcasts.podium.utils

import android.content.Context
import android.telephony.TelephonyManager
import java.security.MessageDigest
import java.util.Locale

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
