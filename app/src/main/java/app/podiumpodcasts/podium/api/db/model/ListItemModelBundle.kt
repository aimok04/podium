package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class ListItemModelBundle(
    @Embedded val listItem: ListItemModel,
    @Relation(
        parentColumn = "contentId",
        entityColumn = "origin"
    )
    val podcast: PodcastModel?,
    @Relation(
        parentColumn = "contentId",
        entityColumn = "id"
    )
    val episode: PodcastEpisodeModel?,
    @Relation(
        parentColumn = "contentId",
        entityColumn = "episodeId"
    )
    val episodePlayState: PodcastEpisodePlayStateModel?,
    @Relation(
        parentColumn = "contentId",
        entityColumn = "episodeId"
    )
    val episodeDownload: PodcastEpisodeDownloadModel? = null
) {
    fun toPodcastEpisodeBundle(): PodcastEpisodeBundle {
        return PodcastEpisodeBundle(
            episode = episode!!,
            playState = episodePlayState,
            download = episodeDownload
        )
    }
}
