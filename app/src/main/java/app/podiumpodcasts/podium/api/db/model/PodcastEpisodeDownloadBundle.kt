package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastEpisodeDownloadBundle(
    @Embedded val download: PodcastEpisodeDownloadModel,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "id"
    )
    val episode: PodcastEpisodeModel,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "episodeId"
    )
    val playState: PodcastEpisodePlayStateModel,
)
