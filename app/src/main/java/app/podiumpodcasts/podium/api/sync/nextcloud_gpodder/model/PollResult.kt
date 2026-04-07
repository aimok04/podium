package app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.model

import kotlinx.serialization.Serializable

interface PollResult {
    @Serializable
    data class Successful(
        val loginName: String,
        val appPassword: String
    ) : PollResult

    object Unsuccessful : PollResult
}