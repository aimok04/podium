package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "listItem",
    foreignKeys = [ForeignKey(
        entity = ListModel::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("listId"),
        onDelete = CASCADE
    )]
)
data class ListItemModel(
    @PrimaryKey(true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("listId")
    val listId: Int,
    @ColumnInfo("contentId")
    val contentId: String,
    @ColumnInfo("isPodcast")
    val isPodcast: Boolean,
    @ColumnInfo("position")
    val position: Int
)