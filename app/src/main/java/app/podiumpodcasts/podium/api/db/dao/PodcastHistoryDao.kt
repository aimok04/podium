package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastHistoryBundle
import app.podiumpodcasts.podium.api.db.model.PodcastHistoryModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastHistoryDao {

    @Query("SELECT * FROM podcastHistory ORDER BY timestamp DESC")
    fun all(): PagingSource<Int, PodcastHistoryBundle>

    @Query("SELECT * FROM podcastHistory WHERE timestamp < :from AND timestamp > :to ORDER BY timestamp DESC")
    fun allIn(from: Long, to: Long): PagingSource<Int, PodcastHistoryBundle>

    @Query("SELECT * FROM podcastHistory WHERE timestamp > :after ORDER BY timestamp DESC")
    fun allAfter(after: Long): PagingSource<Int, PodcastHistoryBundle>

    @Query("SELECT * FROM podcastHistory WHERE timestamp < :before ORDER BY timestamp DESC")
    fun allBefore(before: Long): PagingSource<Int, PodcastHistoryBundle>

    @Query("SELECT * FROM podcastHistory ORDER BY timestamp DESC")
    fun get(): Flow<List<PodcastHistoryBundle>>

    @Query("SELECT * FROM podcastHistory ORDER BY timestamp DESC LIMIT 1")
    fun getLast(): Flow<PodcastHistoryBundle?>

    @Query("UPDATE podcastHistory SET timestamp = :timestamp WHERE episodeId = :episodeId")
    suspend fun updateTimestamp(episodeId: String, timestamp: Long)

    @Query("INSERT INTO podcastHistory (origin,episodeId,timestamp) VALUES (:origin,:episodeId,:timestamp)")
    suspend fun insert(
        origin: String,
        episodeId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun delete(item: PodcastHistoryModel)

}