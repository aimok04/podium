package app.podiumpodcasts.podium.api.gpodder.model.result

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionsGetChangesResult(
    val add: List<String>,
    val remove: List<String>,
    val timestamp: Long
)