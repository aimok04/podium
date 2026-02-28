package app.podiumpodcasts.podium.background.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import app.podiumpodcasts.podium.R
import kotlin.math.roundToInt

class DebugNightlyNotification() : NotificationContainer() {

    override fun notificationId(): Int = (Math.random() * 10000).roundToInt()

    override fun channelId(): String = "debug_nightly_worker"

    override fun createChannel(
        context: Context
    ): NotificationChannel {
        return NotificationChannel(
            channelId(),
            "Nightly worker debug notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Get notified when nightly worker ran"
        }
    }

    override fun createNotification(
        context: Context
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId())
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Nightly worker ran")
            .setContentText("All nightly worker tasks were completed.")
            .setGroup(channelId())
            .setAutoCancel(true)

        return builder.build()
    }

}