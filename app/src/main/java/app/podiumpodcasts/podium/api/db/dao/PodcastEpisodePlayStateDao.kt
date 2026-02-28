package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodePlayStateModel
import app.podiumpodcasts.podium.api.db.model.PodcastPlayStateBundle

@Dao
interface PodcastEpisodePlayStateDao {

    @Query("INSERT INTO podcastEpisodePlayState (episodeId, state, played) VALUES (:episodeId, 0, 0)")
    suspend fun initState(episodeId: String)

    @Query("UPDATE podcastEpisodePlayState SET state = :state WHERE episodeId=:episodeId")
    suspend fun saveState(episodeId: String, state: Int)

    @Query("UPDATE podcastEpisodePlayState SET played = :played WHERE episodeId=:episodeId")
    suspend fun savePlayed(episodeId: String, played: Boolean)

    @Query("SELECT * FROM podcastEpisodePlayState, podcastEpisode e WHERE played=0 AND state>0 AND episodeId=e.id ORDER BY pubDate DESC")
    fun allContinuePlaying(): PagingSource<Int, PodcastPlayStateBundle>

    @Query("SELECT * FROM podcastEpisodePlayState WHERE episodeId=:episodeId")
    suspend fun get(episodeId: String): PodcastEpisodePlayStateModel

}