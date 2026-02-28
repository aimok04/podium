package app.podiumpodcasts.podium.api.db.model.statistics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statisticsUpdatePodcastRun")
data class UpdatePodcastRunModel(
    @PrimaryKey
    @ColumnInfo("timestamp")
    val timestamp: Long,
    @ColumnInfo("dataUsage")
    val dataUsage: Long
)