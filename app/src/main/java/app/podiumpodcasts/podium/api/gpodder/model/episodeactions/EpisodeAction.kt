package app.podiumpodcasts.podium.api.gpodder.model.episodeactions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Serializable
class EpisodeAction(
    @SerialName("action")
    val type: String,
    @SerialName("podcast")
    val podcastOrigin: String,
    @SerialName("episode")
    val episodeAudioUrl: String,
    @SerialName("device")
    val deviceId: String,
    val timestamp: String,
    val started: Int? = null,
    val position: Int? = null,
    val total: Int? = null
) {
    val timestampUnix: Long
        get() {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val dateTime = LocalDateTime.parse(timestamp, formatter)
            return dateTime.toEpochSecond(ZoneOffset.UTC)
        }

}