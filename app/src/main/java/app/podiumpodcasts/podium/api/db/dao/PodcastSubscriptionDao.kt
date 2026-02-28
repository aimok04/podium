package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import app.podiumpodcasts.podium.api.db.model.PodcastSubscriptionBundle
import app.podiumpodcasts.podium.api.db.model.PodcastSubscriptionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastSubscriptionDao {
    @Query("SELECT * FROM podcastSubscription ORDER BY newEpisodes DESC")
    fun allByNewEpisodes(): PagingSource<Int, PodcastSubscriptionBundle>

    @Query("SELECT * FROM podcastSubscription WHERE origin=:origin")
    fun get(origin: String): Flow<PodcastSubscriptionModel?>

    @Query("SELECT * FROM podcastSubscription WHERE origin=:origin")
    suspend fun getSync(origin: String): PodcastSubscriptionModel?

    @Query("SELECT * FROM podcastSubscription ORDER BY lastUpdate ASC")
    suspend fun allSortedByLastUpdate(): List<PodcastSubscriptionBundle>

    @Query("SELECT SUM(p.fileSize) FROM podcastSubscription s, podcast p WHERE p.origin = s.origin")
    suspend fun getEstimatedUpdateDataUsage(): Long?

    @Query("SELECT AVG(p.fileSize) FROM podcastSubscription s, podcast p WHERE p.origin = s.origin")
    suspend fun getAverageDataUsage(): Long?

    @Query("UPDATE podcastSubscription SET cacheLastModified=:lastModified, cacheETag=:eTag, cacheContentLength=:contentLength WHERE origin=:origin")
    suspend fun storeCacheValues(
        origin: String,
        lastModified: String,
        eTag: String,
        contentLength: String
    )

    @Query("UPDATE podcastSubscription SET lastUpdate=:timestamp WHERE origin=:origin")
    suspend fun logUpdate(origin: String, timestamp: Long = System.currentTimeMillis())

    @Query("INSERT INTO podcastSubscription (origin, enableNotifications, enableAutoDownload, lastUpdate, newEpisodes, cacheETag, cacheLastModified, cacheContentLength) VALUES (:origin, 0, 0, 0, 0, '', '', '')")
    suspend fun subscribe(origin: String)

    @Query("DELETE FROM podcastSubscription WHERE origin=:origin")
    suspend fun unsubscribe(origin: String)

    @Query("UPDATE podcastSubscription SET enableNotifications = 1 WHERE origin=:origin")
    suspend fun enableNotifications(origin: String)

    @Query("UPDATE podcastSubscription SET enableNotifications = 0 WHERE origin=:origin")
    suspend fun disableNotifications(origin: String)

    @Query("UPDATE podcastSubscription SET enableAutoDownload = 1 WHERE origin=:origin")
    suspend fun enableAutoDownload(origin: String)

    @Query("UPDATE podcastSubscription SET enableAutoDownload = 0 WHERE origin=:origin")
    suspend fun disableAutoDownload(origin: String)
}