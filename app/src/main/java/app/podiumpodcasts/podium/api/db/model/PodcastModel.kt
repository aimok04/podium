package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcast")
data class PodcastModel(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo("origin")
    val origin: String,
    @ColumnInfo("link")
    val link: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("description")
    val description: String,
    @ColumnInfo("author")
    val author: String,
    @ColumnInfo("imageUrl")
    val imageUrl: String,
    @ColumnInfo("imageSeedColor")
    var imageSeedColor: Int,
    @ColumnInfo("languageCode")
    val languageCode: String,
    @ColumnInfo("fileSize")
    val fileSize: Long,
    @ColumnInfo("overrideTitle")
    val overrideTitle: String = "",
    @ColumnInfo("skipBeginning")
    val skipBeginning: Int = 0,
    @ColumnInfo("skipEnding")
    val skipEnding: Int = 0
) {
    fun fetchTitle(): String {
        return overrideTitle.ifBlank { title }
    }
}