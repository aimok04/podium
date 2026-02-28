package app.podiumpodcasts.podium.background.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.background.notification.DebugNightlyNotification
import app.podiumpodcasts.podium.background.work.DeleteOldDownloadsWork
import app.podiumpodcasts.podium.background.work.DeletePlayedDownloadsWork
import app.podiumpodcasts.podium.background.work.ProcessOrphanDownloadsWork
import app.podiumpodcasts.podium.manager.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NightlyWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(
    context, params
) {
    val db by lazy {
        DatabaseManager.build(context)
    }

    override suspend fun doWork(): Result {
        return try {
            DeleteOldDownloadsWork(applicationContext, db)
                .doWork()

            DeletePlayedDownloadsWork(applicationContext, db)
                .doWork()

            ProcessOrphanDownloadsWork(applicationContext, db)
                .doWork()

            db.statisticsUpdatePodcastRun()
                .cleanUp()

            val settingsRepository = SettingsRepository(applicationContext)
            if(settingsRepository.debug.enableNightlyNotification.first()) {
                DebugNightlyNotification()
                    .send(applicationContext)
            }

            Result.success()
        } catch(e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun scheduleNightlyWork(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if(dueDate.before(currentDate))
                dueDate.add(Calendar.HOUR_OF_DAY, 24)

            val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

            val nightlyRequest = PeriodicWorkRequestBuilder<NightlyWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName = "DailyWorker",
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = nightlyRequest
            )
        }
    }
}