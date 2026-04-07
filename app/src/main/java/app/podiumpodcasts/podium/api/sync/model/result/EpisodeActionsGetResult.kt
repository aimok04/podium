package app.podiumpodcasts.podium.api.sync.model.result

import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeActionsGetResult(
    val actions: List<EpisodeAction>,
    val timestamp: Long
)