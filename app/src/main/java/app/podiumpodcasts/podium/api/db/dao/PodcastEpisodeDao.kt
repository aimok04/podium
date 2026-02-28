package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastEpisodeDao {
    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE origin=:origin ORDER BY pubDate DESC")
    fun all(origin: String): Flow<List<PodcastEpisodeBundle>>

    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE origin=:origin ORDER BY pubDate DESC")
    suspend fun allSync(origin: String): List<PodcastEpisodeBundle>

    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE new=1 ORDER BY pubDate DESC")
    fun allNew(): PagingSource<Int, PodcastEpisodeBundle>

    @Query("SELECT id FROM podcastEpisode WHERE origin=:origin")
    suspend fun getEpisodeIds(origin: String): List<String>

    @Query("SELECT * FROM podcastEpisode WHERE id=:id")
    fun get(id: String): Flow<PodcastEpisodeBundle>

    @Query("SELECT * FROM podcastEpisode WHERE id=:id")
    suspend fun getSync(id: String): PodcastEpisodeBundle

    @Query(
        """
        SELECT * FROM podcastEpisode 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY pubDate DESC
    """
    )
    fun search(query: String): PagingSource<Int, PodcastEpisodeBundle>

    @Transaction
    suspend fun unnewAndUpdateNewEpisodesCount(
        origin: String, episodeId: String
    ) {
        _unnew(episodeId)
        _updateNewEpisodesCount(origin)
    }

    @Transaction
    suspend fun insertAllAndUpdateNewEpisodesCount(
        origin: String,
        vararg episodes: PodcastEpisodeModel
    ) {
        _insertAll(*episodes)
        _updateNewEpisodesCount(origin)
    }

    @Update
    suspend fun update(episode: PodcastEpisodeModel)

    @Query("DELETE FROM podcastEpisode WHERE id=:id")
    suspend fun delete(id: String)

    @Insert
    suspend fun _insertAll(vararg episodes: PodcastEpisodeModel)

    @Query("UPDATE podcastEpisode SET new=0 WHERE id=:id")
    suspend fun _unnew(id: String)

    @Query(
        """
        UPDATE podcastSubscription 
        SET newEpisodes = (
            SELECT COUNT(*) 
            FROM podcastEpisode 
            WHERE podcastEpisode.origin = podcastSubscription.origin 
            AND podcastEpisode.new = 1
        ) WHERE origin=:origin
    """
    )
    suspend fun _updateNewEpisodesCount(origin: String)
}