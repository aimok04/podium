package app.podiumpodcasts.podium.api.sync.model.episodeactions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Serializable
class EpisodeAction(
    @SerialName("action")
    private val _type: String,
    @SerialName("podcast")
    val podcastOrigin: String,
    @SerialName("episode")
    val episodeAudioUrl: String,
    @SerialName("device")
    val deviceId: String = "",
    val timestamp: String,
    val started: Int? = null,
    val position: Int? = null,
    val total: Int? = null
) {
    @SerialName("action")
    val type: String
        get() = _type.lowercase()

    val timestampUnix: Long
        get() {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTime = LocalDateTime.parse(timestamp, formatter)
                return dateTime.toEpochSecond(ZoneOffset.UTC)
            } catch(e: Exception) {
                // fallback for nextcloud-gpodder
                return OffsetDateTime.parse(timestamp).toEpochSecond()
            }
        }

}