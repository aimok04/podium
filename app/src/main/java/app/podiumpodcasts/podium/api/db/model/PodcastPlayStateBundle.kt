package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastPlayStateBundle(
    @Embedded val playState: PodcastEpisodePlayStateModel,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "id"
    )
    val episode: PodcastEpisodeModel,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "episodeId"
    )
    val download: PodcastEpisodeDownloadModel? = null
) {
    fun toPodcastEpisodeBundle(): PodcastEpisodeBundle {
        return PodcastEpisodeBundle(
            episode = episode,
            playState = playState,
            download = download
        )
    }
}