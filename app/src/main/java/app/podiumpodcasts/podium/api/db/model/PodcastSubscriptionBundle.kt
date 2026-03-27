package app.podiumpodcasts.podium.api.db.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Embedded
import androidx.room.Relation

data class PodcastSubscriptionBundle(
    @Embedded val subscription: PodcastSubscriptionModel,
    @Relation(
        parentColumn = "origin",
        entityColumn = "origin"
    )
    val podcast: PodcastModel
) {
    fun createMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId("podcast:${subscription.origin}")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(podcast.title)
                    .setDescription(podcast.description)
                    .setArtist(podcast.author)
                    .setArtworkUri(Uri.parse(podcast.imageUrl))
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }
}