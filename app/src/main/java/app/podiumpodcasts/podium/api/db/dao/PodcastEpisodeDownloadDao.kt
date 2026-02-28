package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadState
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastEpisodeDownloadDao {
    @Transaction
    @Query("SELECT * FROM podcastEpisodeDownload ORDER BY state ASC, timestamp DESC")
    fun all(): PagingSource<Int, PodcastEpisodeDownloadBundle>

    @Transaction
    @Query("SELECT d.* FROM podcastEpisodeDownload d, podcastEpisodePlayState p WHERE d.episodeId = p.episodeId AND p.played ORDER BY d.timestamp DESC")
    suspend fun allPlayedByTimestamp(): List<PodcastEpisodeDownloadBundle>

    @Query("SELECT * FROM podcastEpisodeDownload ORDER BY RANDOM()")
    suspend fun allRandomSync(): List<PodcastEpisodeDownloadBundle>

    @Query("SELECT * FROM podcastEpisodeDownload WHERE state != :downloadedStateValue")
    suspend fun allNotDownloadedSync(downloadedStateValue: Int = PodcastEpisodeDownloadState.DOWNLOADED.value): List<PodcastEpisodeDownloadModel>

    @Query("SELECT * FROM podcastEpisodeDownload WHERE episodeId=:episodeId")
    fun get(episodeId: String): Flow<PodcastEpisodeDownloadBundle?>

    @Query("SELECT * FROM podcastEpisodeDownload WHERE episodeId=:episodeId")
    suspend fun getSync(episodeId: String): PodcastEpisodeDownloadModel?

    @Query("SELECT * FROM podcastEpisodeDownload WHERE timestamp<:timestamp")
    suspend fun getOlderThanSync(timestamp: Long): List<PodcastEpisodeDownloadBundle>

    @Query("SELECT SUM(size) FROM podcastEpisodeDownload")
    fun totalSize(): Flow<Long>

    @Query("INSERT INTO podcastEpisodeDownload (episodeId,state,progress,size,timestamp) VALUES (:episodeId,0,0,0,0)")
    suspend fun add(episodeId: String)

    @Query("UPDATE podcastEpisodeDownload SET state=:stateValue WHERE episodeId=:episodeId")
    suspend fun setState(episodeId: String, stateValue: Int)

    @Query("UPDATE podcastEpisodeDownload SET progress=:progress,size=:size WHERE episodeId=:episodeId")
    fun setProgress(episodeId: String, progress: Long, size: Long)

    @Query("UPDATE podcastEpisodeDownload SET state=2, filename=:filename, timestamp=:timestamp WHERE episodeId=:episodeId")
    suspend fun registerDownload(
        episodeId: String,
        filename: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM podcastEpisodeDownload WHERE episodeId=:episodeId")
    suspend fun delete(episodeId: String)
}