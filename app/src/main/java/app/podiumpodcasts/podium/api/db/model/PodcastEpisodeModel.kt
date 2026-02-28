package app.podiumpodcasts.podium.api.db.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import app.podiumpodcasts.podium.manager.DownloadManager
import app.podiumpodcasts.podium.utils.sha256
import java.io.File

enum class MediaMetadataExtra {
    ORIGIN,
    EPISODE_ID,
    IMAGE_SEED_COLOR,
    RESUME_AT,
    IS_DOWNLOAD,
    SKIP_BEGINNING,
    SKIP_ENDING
}

@Entity(
    tableName = "podcastEpisode",
    foreignKeys = [ForeignKey(
        entity = PodcastModel::class,
        parentColumns = arrayOf("origin"),
        childColumns = arrayOf("origin"),
        onDelete = CASCADE
    )]
)
data class PodcastEpisodeModel(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("guid")
    val guid: String,
    @ColumnInfo("origin")
    val origin: String,
    @ColumnInfo("link")
    val link: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("description")
    val description: String,
    @ColumnInfo("imageUrl")
    var imageUrl: String?,
    @ColumnInfo("author")
    val author: String,
    @ColumnInfo("pubDate")
    val pubDate: Long,
    @ColumnInfo("duration")
    val duration: Int,
    @ColumnInfo("audioUrl")
    val audioUrl: String,
    @ColumnInfo("podcastTitle")
    val podcastTitle: String,
    @ColumnInfo("imageSeedColor")
    var imageSeedColor: Int,
    @ColumnInfo("new")
    val new: Boolean = false
) {
    fun createMediaItem(
        context: Context,
        resumeAt: Long? = null
    ): MediaItem {
        val downloadFile = craftDownloadFile(context)

        return MediaItem.Builder()
            .setMediaMetadata(
                createMediaMetadata(
                    resumeAt = resumeAt,
                    isDownload = downloadFile.exists()
                )
            )
            .setUri(
                when(downloadFile.exists()) {
                    true -> Uri.fromFile(downloadFile)
                    false -> Uri.parse(audioUrl)
                }
            ).build()
    }

    fun createMediaMetadata(
        resumeAt: Long? = null,
        isDownload: Boolean
    ): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(title)
            .setDescription(description)
            .setArtist(podcastTitle)
            .setSubtitle(podcastTitle)
            .setDisplayTitle(title)
            .setArtworkUri((imageUrl ?: "").toUri())
            .setExtras(
                Bundle().apply {
                    putString(MediaMetadataExtra.ORIGIN.name, origin)
                    putString(MediaMetadataExtra.EPISODE_ID.name, id)
                    putInt(MediaMetadataExtra.IMAGE_SEED_COLOR.name, imageSeedColor)
                    putBoolean(MediaMetadataExtra.IS_DOWNLOAD.name, isDownload)
                    if(resumeAt != null) putLong(MediaMetadataExtra.RESUME_AT.name, resumeAt)
                }
            )
            .build()
    }

    fun craftDownloadFile(
        context: Context
    ): File {
        val podcastDownloadsDir =
            File(DownloadManager.getDownloadsDirectory(context), origin.sha256())
        podcastDownloadsDir.mkdirs()
        return File(podcastDownloadsDir, audioUrl.sha256())
    }
}