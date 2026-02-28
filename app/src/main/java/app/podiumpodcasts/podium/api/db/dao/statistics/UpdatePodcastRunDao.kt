package app.podiumpodcasts.podium.api.db.dao.statistics

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UpdatePodcastRunDao {
    @Query("SELECT AVG(dataUsage) FROM statisticsUpdatePodcastRun")
    suspend fun getAvgDataUsage(): Long?

    @Query("INSERT INTO statisticsUpdatePodcastRun (dataUsage, timestamp) VALUES (:dataUsage, :timestamp)")
    suspend fun log(dataUsage: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM statisticsUpdatePodcastRun WHERE timestamp < :timestampOlder")
    suspend fun cleanUp(
        // default: delete entries older than a week (604800000)
        timestampOlder: Long = System.currentTimeMillis() - 604800000
    )

    @Query("DELETE FROM statisticsUpdatePodcastRun")
    suspend fun clear()
}