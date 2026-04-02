package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadState
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodePlayStateModel
import kotlinx.coroutines.flow.Flow

enum class PodcastEpisodesOrder(
    val value: String
) {
    ASCENDING("ASC"),
    DESCENDING("DESC")
}

enum class PodcastEpisodesOrderBy(
    val value: String
) {
    DATE("e.pubDate"),
    TITLE("e.title"),
    DURATION("e.duration")
}

enum class PodcastEpisodesFilter(
    val condition: String
) {
    PLAYED("p.played = 1"),
    PAUSED("p.played = 0 AND p.state > 0"),
    NEW("e.new = 1"),
    FAVORITE("EXISTS(SELECT 1 FROM listItem WHERE contentId = e.id AND listId = -2)"),
    DOWNLOADED("COALESCE(d.state, 0) = ${PodcastEpisodeDownloadState.DOWNLOADED.value}")
}

@Dao
interface PodcastEpisodeDao {
    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE origin=:origin ORDER BY pubDate DESC")
    fun all(origin: String): Flow<List<PodcastEpisodeBundle>>

    @RawQuery(
        observedEntities = [
            PodcastEpisodeModel::class,
            PodcastEpisodePlayStateModel::class,
            PodcastEpisodeDownloadModel::class
        ]
    )
    fun queryPaged(query: SupportSQLiteQuery): PagingSource<Int, PodcastEpisodeBundle>

    fun buildQuery(
        origin: String,
        searchQuery: String?,
        orderBy: PodcastEpisodesOrderBy,
        order: PodcastEpisodesOrder,
        filter: Set<PodcastEpisodesFilter>,
        filterNot: Set<PodcastEpisodesFilter>
    ): SimpleSQLiteQuery {
        val builder = StringBuilder(
            """
            SELECT e.* FROM podcastEpisode AS e
            LEFT JOIN podcastEpisodePlayState AS p ON e.id = p.episodeId
            LEFT JOIN podcastEpisodeDownload AS d ON e.id = d.episodeId
            WHERE e.origin = ?
        """.trimIndent()
        )

        val bindArgs = mutableListOf<Any>(origin)

        if(!searchQuery.isNullOrBlank()) {
            builder.append(" AND (e.title LIKE ? OR e.description LIKE ?)")

            val wildcard = "%$searchQuery%"
            bindArgs.add(wildcard)
            bindArgs.add(wildcard)
        }

        filter.forEach { builder.append(" AND ${it.condition}") }
        filterNot.forEach { builder.append(" AND NOT( ${it.condition} )") }

        builder.append(" ORDER BY ${orderBy.value} ${order.value}")
        return SimpleSQLiteQuery(builder.toString(), bindArgs.toTypedArray())
    }

    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE origin=:origin ORDER BY pubDate DESC")
    suspend fun allSync(origin: String): List<PodcastEpisodeBundle>

    @Query("SELECT * FROM podcastEpisode WHERE origin=:origin ORDER BY pubDate DESC LIMIT :limit OFFSET :offset")
    suspend fun get(origin: String, limit: Int, offset: Int): List<PodcastEpisodeBundle>

    @Transaction
    @Query("SELECT * FROM podcastEpisode WHERE new=1 ORDER BY pubDate DESC")
    fun allNew(): PagingSource<Int, PodcastEpisodeBundle>

    @Query("SELECT * FROM podcastEpisode WHERE new=1 ORDER BY pubDate DESC LIMIT :limit OFFSET :offset")
    fun getNew(limit: Int, offset: Int): List<PodcastEpisodeBundle>

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
    suspend fun newAndUpdateNewEpisodesCount(
        origin: String, episodeId: String
    ) {
        _new(episodeId)
        _updateNewEpisodesCount(origin)
    }

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

    @Query("UPDATE podcastEpisode SET new=1 WHERE id=:id")
    suspend fun _new(id: String)

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