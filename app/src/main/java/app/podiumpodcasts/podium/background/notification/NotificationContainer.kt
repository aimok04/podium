package app.podiumpodcasts.podium.background.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

abstract class NotificationContainer {

    abstract fun notificationId(): Int

    abstract fun channelId(): String

    abstract fun createChannel(context: Context): NotificationChannel

    abstract fun createNotification(context: Context): Notification

    fun send(
        context: Context
    ) {
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.createNotificationChannel(createChannel(context))
        notificationManager.notify(notificationId(), createNotification(context))
    }

    companion object {
        fun cancel(
            context: Context,
            id: Int
        ) {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(id)
        }
    }

}