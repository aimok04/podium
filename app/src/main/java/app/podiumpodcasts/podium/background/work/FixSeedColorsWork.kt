package app.podiumpodcasts.podium.background.work

import android.content.Context
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import app.podiumpodcasts.podium.api.db.AppDatabase
import coil3.Bitmap
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.materialkolor.ktx.themeColorOrNull


class FixSeedColorsWork(
    val context: Context,
    val db: AppDatabase
) {

    suspend fun doWork() {
        val podcasts = db.podcasts().allSync()

        for(podcast in podcasts) {
            val image = loadBitmap(podcast.imageUrl)

            val color = image?.asImageBitmap()?.themeColorOrNull()
            podcast.imageSeedColor = color?.toArgb() ?: 0

            db.podcasts().update(podcast)
            val episodes = db.podcastEpisodes().allSync(podcast.origin)
            episodes.forEach {
                it.episode.imageSeedColor = podcast.imageSeedColor
                if(it.episode.imageUrl == null) it.episode.imageUrl = podcast.imageUrl

                db.podcastEpisodes().update(it.episode)
            }
        }
    }

    suspend fun loadBitmap(imageUrl: String): Bitmap? {
        val loader = ImageLoader(context)

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if(result is ErrorResult) {
            result.throwable.printStackTrace()
            return null
        } else if(result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            return bitmap
        }

        return null
    }

}