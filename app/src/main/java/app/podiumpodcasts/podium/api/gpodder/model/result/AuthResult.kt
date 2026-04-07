package app.podiumpodcasts.podium.api.gpodder.model.result

import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val cookie: String
)