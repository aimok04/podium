package app.podiumpodcasts.podium.api.sync.model.result

import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val cookie: String
)