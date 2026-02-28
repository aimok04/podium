package app.podiumpodcasts.podium.api.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.podiumpodcasts.podium.api.db.model.ListModel
import app.podiumpodcasts.podium.api.db.model.ListWithContains
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    @Query("SELECT * FROM list WHERE isSystemList = 1")
    fun allSystem(): PagingSource<Int, ListModel>

    @Query("SELECT * FROM list WHERE isSystemList = 0 ORDER BY createdAt DESC")
    fun allNonSystem(): PagingSource<Int, ListModel>

    @Query(
        """
        SELECT 
            list.*, 
            EXISTS(
                SELECT 1 FROM listItem 
                WHERE listItem.listId = list.id AND listItem.contentId = :contentId
            ) AS contains 
        FROM list WHERE id != -2
    """
    )
    fun allWithContains(contentId: String): PagingSource<Int, ListWithContains>

    @Query("SELECT * FROM list WHERE id = :id")
    fun get(id: Int): Flow<ListModel?>

    @Insert
    suspend fun create(list: ListModel): Long

    @Query("INSERT OR IGNORE INTO list (id, name, description, itemCount, createdAt, isSystemList) VALUES (-1, 'HEAR_LATER', 'HEAR_LATER', 0, 0, 1)")
    suspend fun createHearLater()

    @Query("INSERT OR IGNORE INTO list (id, name, description, itemCount, createdAt, isSystemList) VALUES (-2, 'FAVORITES', 'FAVORITES', 0, 0, 1)")
    suspend fun createFavorites()

    @Query("UPDATE list SET name = :name, description = :description WHERE id = :id")
    suspend fun edit(id: Int, name: String, description: String)

    @Update
    suspend fun update(list: ListModel)

    @Query("DELETE FROM list WHERE id = :id")
    suspend fun delete(id: Int)

}