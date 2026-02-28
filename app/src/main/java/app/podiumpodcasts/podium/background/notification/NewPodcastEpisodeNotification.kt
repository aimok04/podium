package app.podiumpodcasts.podium.background.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.DeepLink
import app.podiumpodcasts.podium.ui.asPendingIntent

class NewPodcastEpisodeNotification(
    val podcastTitle: String,
    val episode: PodcastEpisodeModel,
    val imageBitmap: Bitmap? = null
) : NotificationContainer() {

    override fun notificationId(): Int = episode.id.hashCode()

    override fun channelId(): String = "new_podcast_episode"

    override fun createChannel(
        context: Context
    ): NotificationChannel {

        return NotificationChannel(
            channelId(),
            context.getString(R.string.notifications_new_podcast_episodes_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                context.getString(R.string.notifications_new_podcast_episodes_channel_description)
            enableVibration(true)
        }
    }

    override fun createNotification(
        context: Context
    ): Notification {
        val deepLink = DeepLink.OpenPodcastEpisode(
            origin = episode.origin,
            episodeId = episode.id
        )

        val builder = NotificationCompat.Builder(context, channelId())
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setLargeIcon(imageBitmap)
            .setContentTitle(
                context.getString(
                    R.string.notifications_new_podcast_episodes_title,
                    podcastTitle
                )
            )
            .setContentText(episode.title)
            .setGroup(channelId())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(deepLink.asPendingIntent(context, notificationId()))
            .setAutoCancel(true)

        if(imageBitmap != null)
            builder.setLargeIcon(imageBitmap)

        return builder.build()
    }

    companion object {
        fun cancel(context: Context, episodeId: String) {
            cancel(context, episodeId.hashCode())
        }
    }

}