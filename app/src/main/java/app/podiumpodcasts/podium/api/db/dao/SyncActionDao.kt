package app.podiumpodcasts.podium.api.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import app.podiumpodcasts.podium.api.db.model.SyncActionModel
import app.podiumpodcasts.podium.api.db.model.SyncActionType

@Dao
interface SyncActionDao {

    @Query("SELECT * FROM syncAction WHERE timestamp <= :timestamp")
    suspend fun allBefore(timestamp: Long): List<SyncActionModel>

    @Transaction
    suspend fun addPlayState(
        origin: String,
        episodeId: String,
        audioUrl: String,
        duration: Int,
        state: Int,
        played: Boolean
    ) {
        delete("play:$episodeId")
        _insert(
            SyncActionModel(
                id = "play:$episodeId",
                actionType = SyncActionType.PLAY.name,
                origin = origin,
                audioUrl = audioUrl,
                position = state,
                total = duration,
                timestamp = System.currentTimeMillis() / 1000L
            )
        )
    }

    @Transaction
    suspend fun addSubscribe(
        origin: String
    ) {
        delete(origin)
        _insert(
            SyncActionModel(
                id = origin,
                actionType = SyncActionType.SUBSCRIBE.name,
                origin = origin,
                timestamp = System.currentTimeMillis() / 1000L
            )
        )
    }

    @Transaction
    suspend fun addUnsubscribe(
        origin: String
    ) {
        delete(origin)
        _insert(
            SyncActionModel(
                id = origin,
                actionType = SyncActionType.UNSUBSCRIBE.name,
                origin = origin,
                timestamp = System.currentTimeMillis() / 1000L
            )
        )
    }

    @Query("DELETE FROM syncAction WHERE timestamp <= :timestamp")
    suspend fun deleteBefore(timestamp: Long)

    @Query("DELETE FROM syncAction WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM syncAction")
    suspend fun clear()

    @Insert
    suspend fun _insert(model: SyncActionModel)

}