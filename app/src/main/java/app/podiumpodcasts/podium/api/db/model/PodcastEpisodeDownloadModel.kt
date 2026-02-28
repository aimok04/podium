package app.podiumpodcasts.podium.api.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.utils.sha256
import java.io.File

enum class PodcastEpisodeDownloadState(
    val value: Int,
    val label: Int
) {
    NOT_DOWNLOADED(0, R.string.common_waiting),
    DOWNLOADING(1, R.string.common_downloading),
    DOWNLOADED(2, R.string.common_downloaded)
}

@Entity("podcastEpisodeDownload")
data class PodcastEpisodeDownloadModel(
    @PrimaryKey
    @ColumnInfo("episodeId")
    val episodeId: String,
    @ColumnInfo("state")
    val state: Int,
    @ColumnInfo("filename")
    val filename: String? = null,
    @ColumnInfo("progress")
    val progress: Long,
    @ColumnInfo("size")
    val size: Long,
    @ColumnInfo("timestamp")
    val timestamp: Long
) {
    fun parseState(): PodcastEpisodeDownloadState {
        return PodcastEpisodeDownloadState.entries.find { it.value == state }!!
    }
}