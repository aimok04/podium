package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "podcastEpisodePlayState",
    foreignKeys = [ForeignKey(
        entity = PodcastEpisodeModel::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("episodeId"),
        onDelete = CASCADE
    )]
)
data class PodcastEpisodePlayStateModel(
    @PrimaryKey
    @ColumnInfo("episodeId")
    val episodeId: String,
    @ColumnInfo("state")
    val state: Int = 0,
    @ColumnInfo("played")
    val played: Boolean = false
)