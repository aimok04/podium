package app.podiumpodcasts.podium.api.db.model

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
)
