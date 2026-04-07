package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodePlayStateBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodePlayStateModel
import app.podiumpodcasts.podium.api.db.model.PodcastPlayStateBundle

@Dao
interface PodcastEpisodePlayStateDao {

    @Query("SELECT * FROM podcastEpisodePlayState WHERE state > 0 OR played != 0")
    suspend fun allSync(): List<PodcastEpisodePlayStateBundle>

    @Query("INSERT INTO podcastEpisodePlayState (episodeId, state, played, lastUpdate) VALUES (:episodeId, 0, 0, 0)")
    suspend fun initState(episodeId: String)

    @Query("UPDATE podcastEpisodePlayState SET state = :state, lastUpdate = :lastUpdate WHERE episodeId=:episodeId")
    suspend fun saveState(
        episodeId: String,
        state: Int,
        lastUpdate: Long = System.currentTimeMillis() / 1000L
    )

    @Query("UPDATE podcastEpisodePlayState SET played = :played, lastUpdate = :lastUpdate WHERE episodeId=:episodeId")
    suspend fun savePlayed(
        episodeId: String,
        played: Boolean,
        lastUpdate: Long = System.currentTimeMillis() / 1000L
    )

    @Query("UPDATE podcastEpisodePlayState SET state = :state, played = :played, lastUpdate = :lastUpdate WHERE episodeId=:episodeId")
    suspend fun set(
        episodeId: String,
        state: Int,
        played: Boolean,
        lastUpdate: Long = System.currentTimeMillis() / 1000L
    )

    @Query("SELECT * FROM podcastEpisodePlayState WHERE played=0 AND state>0 ORDER BY lastUpdate DESC")
    fun allContinuePlaying(): PagingSource<Int, PodcastPlayStateBundle>

    @Query("SELECT * FROM podcastEpisodePlayState, podcastEpisode e WHERE played=0 AND state>0 AND episodeId=e.id ORDER BY pubDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getContinuePlayingSync(limit: Int, offset: Int): List<PodcastPlayStateBundle>

    @Query("SELECT * FROM podcastEpisodePlayState WHERE episodeId=:episodeId")
    suspend fun get(episodeId: String): PodcastEpisodePlayStateModel

}