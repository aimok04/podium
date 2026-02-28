package app.podiumpodcasts.podium.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.podiumpodcasts.podium.api.db.model.MediaMetadataExtra


fun MediaMetadata.getOrigin(): String {
    return extras?.getString(MediaMetadataExtra.ORIGIN.name)!!
}

fun MediaMetadata.getEpisodeId(): String {
    return extras?.getString(MediaMetadataExtra.EPISODE_ID.name)!!
}

fun MediaMetadata.getImageSeedColor(): Int {
    return extras?.getInt(MediaMetadataExtra.IMAGE_SEED_COLOR.name)!!
}

fun MediaMetadata.isDownload(): Boolean {
    return extras?.getBoolean(MediaMetadataExtra.IS_DOWNLOAD.name)!!
}

fun MediaMetadata.getResumeAt(): Long? {
    return extras?.getLong(MediaMetadataExtra.RESUME_AT.name)?.let {
        when(it) {
            0L -> null
            else -> it
        }
    }
}

fun MediaItem.getOrigin(): String {
    return mediaMetadata.getOrigin()
}

fun MediaItem.getEpisodeId(): String {
    return mediaMetadata.getEpisodeId()
}

fun MediaItem.getImageSeedColor(): Int {
    return mediaMetadata.getImageSeedColor()
}

fun MediaItem.isDownload(): Boolean {
    return mediaMetadata.isDownload()
}

fun MediaItem.getResumeAt(): Long? {
    return mediaMetadata.getResumeAt()
}