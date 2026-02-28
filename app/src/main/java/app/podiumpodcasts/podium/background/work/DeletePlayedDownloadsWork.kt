package app.podiumpodcasts.podium.background.work

import android.content.Context
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.manager.DownloadManager
import kotlinx.coroutines.flow.first

class DeletePlayedDownloadsWork(
    val context: Context,
    val db: AppDatabase,
    val settingsRepository: SettingsRepository = SettingsRepository(context)
) {

    suspend fun doWork(): Boolean {
        if(settingsRepository.behavior.deletePlayedDownloads.first()) {
            val bundles = db.podcastEpisodeDownloads().allPlayedByTimestamp()

            for(bundle in bundles) {
                if(!bundle.playState.played) continue

                DownloadManager.deleteEpisodeDownload(
                    context = context,
                    db = db,
                    episode = bundle.episode
                )
            }
        }

        return true
    }

}