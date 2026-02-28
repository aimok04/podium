package app.podiumpodcasts.podium

import android.app.Application
import android.content.Context
import app.podiumpodcasts.podium.background.worker.NightlyWorker
import app.podiumpodcasts.podium.background.worker.PeriodicPodcastUpdateWorker
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class App : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        val settingsRepository = SettingsRepository(this)

        MainScope().launch {
            PeriodicPodcastUpdateWorker.enqueueWorker(
                context = this@App,
                settingsRepository = settingsRepository,
                replace = false
            )
        }

        NightlyWorker.scheduleNightlyWork(this)
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .logger(DebugLogger())
            .crossfade(true)
            .build()
    }
}