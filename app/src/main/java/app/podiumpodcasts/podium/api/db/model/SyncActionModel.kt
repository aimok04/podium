package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.utils.unixSecondsToIso8601

enum class SyncActionType {
    PLAY,
    SUBSCRIBE,
    UNSUBSCRIBE
}

@Entity(
    tableName = "syncAction"
)
data class SyncActionModel(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("actionType")
    val actionType: String,
    @ColumnInfo("origin")
    val origin: String,
    @ColumnInfo("audioUrl")
    val audioUrl: String? = null,
    @ColumnInfo("position")
    val position: Int? = null,
    @ColumnInfo("total")
    val total: Int? = null,
    @ColumnInfo("timestamp")
    val timestamp: Long
) {
    fun toEpisodeAction(
        deviceId: String
    ): EpisodeAction {
        if(actionType != SyncActionType.PLAY.name)
            throw Exception("actionType must be PLAY")

        return EpisodeAction(
            _type = "play",
            podcastOrigin = origin,
            episodeAudioUrl = audioUrl!!,
            deviceId = deviceId,
            timestamp = unixSecondsToIso8601(timestamp) ?: "",
            started = 0,
            position = position,
            total = total
        )
    }

}