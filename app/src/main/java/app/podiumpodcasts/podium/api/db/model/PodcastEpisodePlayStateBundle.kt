package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation
import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.utils.unixSecondsToIso8601
import kotlin.time.ExperimentalTime

data class PodcastEpisodePlayStateBundle(
    @Embedded val playState: PodcastEpisodePlayStateModel,
    @Relation(
        parentColumn = "episodeId",
        entityColumn = "id"
    )
    val episode: PodcastEpisodeModel
) {
    @OptIn(ExperimentalTime::class)
    fun toEpisodeAction(
        deviceId: String
    ): EpisodeAction {
        return EpisodeAction(
            _type = "play",
            podcastOrigin = episode.origin,
            episodeAudioUrl = episode.audioUrl,
            deviceId = deviceId,
            timestamp = unixSecondsToIso8601(playState.lastUpdate) ?: "",
            started = 0,
            position = if(playState.played)
                episode.duration
            else
                playState.state,
            total = episode.duration
        )
    }
}