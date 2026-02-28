package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcast")
    fun all(): PagingSource<Int, PodcastModel>

    @Query("SELECT * FROM podcast")
    suspend fun allSync(): List<PodcastModel>

    @Query("SELECT * FROM podcast WHERE origin=:origin")
    fun get(origin: String): Flow<PodcastModel>

    @Query("SELECT * FROM podcast WHERE origin=:origin")
    suspend fun getSync(origin: String): PodcastModel?

    @Query(
        """
        SELECT * FROM podcast
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%'
    """
    )
    fun search(query: String): PagingSource<Int, PodcastModel>

    @Query("UPDATE podcast SET fileSize=:fileSize WHERE origin=:origin")
    suspend fun updateFileSize(origin: String, fileSize: Long)

    @Query("UPDATE podcast SET overrideTitle=:overrideTitle WHERE origin=:origin")
    suspend fun setOverrideTitle(origin: String, overrideTitle: String)

    @Query("UPDATE podcast SET skipBeginning=:skipBeginning WHERE origin=:origin")
    suspend fun setSkipBeginning(origin: String, skipBeginning: Int)

    @Query("UPDATE podcast SET skipEnding=:skipEnding WHERE origin=:origin")
    suspend fun setSkipEnding(origin: String, skipEnding: Int)

    @Insert
    suspend fun insertAll(vararg podcasts: PodcastModel)

    @Update
    suspend fun update(podcast: PodcastModel)

    @Delete
    suspend fun delete(podcast: PodcastModel)
}