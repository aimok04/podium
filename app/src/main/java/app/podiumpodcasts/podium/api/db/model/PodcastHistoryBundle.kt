package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastHistoryBundle(
    @Embedded val history: PodcastHistoryModel,
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
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "episodeId"
    )
    val download: PodcastEpisodeDownloadModel? = null
)
