package app.podiumpodcasts.podium.api.db.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.podiumpodcasts.podium.R

enum class SystemLists(
    val id: Int,
    val label: Int,
    val icon: ImageVector,
    val onlyEpisodes: Boolean,
    val hideFromSaveToList: Boolean = false
) {
    HEAR_LATER(-1, R.string.system_lists_hear_later_label, Icons.Rounded.WatchLater, true),
    FAVORITES(-2, R.string.system_lists_favorites_label, Icons.Rounded.Star, true, true);
}

@Entity(
    tableName = "list"
)
data class ListModel(
    @PrimaryKey(true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("description")
    val description: String,
    @ColumnInfo("itemCount")
    val itemCount: Int = 0,
    @ColumnInfo("imageUrls")
    val imageUrls: String? = null, // separated by \n for simplicity
    @ColumnInfo("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("isSystemList")
    val isSystemList: Boolean = false
) {
    fun systemList(): SystemLists? {
        if(!isSystemList) return null
        return SystemLists.entries.first { it.id == id }
    }

    fun getImageUrls(): List<String> {
        return imageUrls?.split("\n")
            ?: listOf()
    }
}