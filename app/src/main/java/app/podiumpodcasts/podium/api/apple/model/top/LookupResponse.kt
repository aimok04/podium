package app.podiumpodcasts.podium.api.apple.model.top

import kotlinx.serialization.Serializable

@Serializable
data class LookupResponse(
    val resultCount: Long,
    val results: List<LookupResult>,
)

@Serializable
data class LookupResult(
    val feedUrl: String
)