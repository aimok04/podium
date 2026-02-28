package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "podcastHistory",
    foreignKeys = [ForeignKey(
        entity = PodcastEpisodeModel::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("episodeId"),
        onDelete = CASCADE
    )]
)
data class PodcastHistoryModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo("origin")
    val origin: String,
    @ColumnInfo("episodeId")
    val episodeId: String,
    @ColumnInfo("timestamp")
    val timestamp: Long
)