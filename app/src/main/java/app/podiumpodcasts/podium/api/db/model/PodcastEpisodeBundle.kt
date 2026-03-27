package app.podiumpodcasts.podium.api.db.model

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.room.Embedded
import androidx.room.Relation

data class PodcastEpisodeBundle(
    @Embedded val episode: PodcastEpisodeModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "episodeId"
    )
    val playState: PodcastEpisodePlayStateModel?,
    @Relation(
        parentColumn = "id",
        entityColumn = "episodeId"
    )
    val download: PodcastEpisodeDownloadModel? = null
) {
    fun createMediaItem(
        context: Context
    ): MediaItem {
        return episode.createMediaItem(context, playState)
    }
}
