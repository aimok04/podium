package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "podcastSubscription",
    foreignKeys = [ForeignKey(
        entity = PodcastModel::class,
        parentColumns = arrayOf("origin"),
        childColumns = arrayOf("origin"),
        onDelete = CASCADE
    )]
)
data class PodcastSubscriptionModel(
    @PrimaryKey
    @ColumnInfo("origin")
    val origin: String,
    @ColumnInfo("enableNotifications")
    val enableNotifications: Boolean,
    @ColumnInfo("enableAutoDownload")
    val enableAutoDownload: Boolean,
    @ColumnInfo("lastUpdate")
    val lastUpdate: Long = 0,
    @ColumnInfo("newEpisodes")
    val newEpisodes: Int = 0,
    @ColumnInfo("cacheETag")
    val cacheETag: String = "",
    @ColumnInfo("cacheLastModified")
    val cacheLastModified: String = "",
    @ColumnInfo("cacheContentLength")
    val cacheContentLength: String = ""
)