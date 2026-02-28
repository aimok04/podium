package app.podiumpodcasts.podium.background.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.background.work.PodcastUpdateWork
import app.podiumpodcasts.podium.manager.DatabaseManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PeriodicPodcastUpdateWorker(
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
            val worker = PodcastUpdateWork(applicationContext, db)
            worker.doWork()

            Result.success()
        } catch(e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        suspend fun enqueueWorker(
            context: Context,
            settingsRepository: SettingsRepository,
            replace: Boolean = false,
            delay: Boolean = false
        ) {
            val intervalMinutes = settingsRepository.behavior.updatePodcastsIntervalMinutes.first()
            val inRoaming = settingsRepository.behavior.updatePodcastsInRoaming.first()

            enqueueWorker(
                context = context,
                repeatIntervalMinutes = intervalMinutes.toLong(),
                inRoaming = inRoaming,
                replace = replace,
                delay = delay
            )
        }

        fun enqueueWorker(
            context: Context,
            repeatIntervalMinutes: Long,
            inRoaming: Boolean,
            replace: Boolean = false,
            delay: Boolean = false
        ) {
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    uniqueWorkName = "PeriodicPodcastUpdateWorker",
                    existingPeriodicWorkPolicy = when(replace) {
                        true -> ExistingPeriodicWorkPolicy.REPLACE
                        false -> ExistingPeriodicWorkPolicy.KEEP
                    },
                    request = PeriodicWorkRequestBuilder<PeriodicPodcastUpdateWorker>(
                        repeatIntervalMinutes,
                        TimeUnit.MINUTES
                    )
                        .setConstraints(
                            Constraints(
                                requiredNetworkType = when(inRoaming) {
                                    true -> NetworkType.CONNECTED
                                    false -> NetworkType.NOT_ROAMING
                                },
                                requiresBatteryNotLow = true
                            )
                        ).let {
                            if(delay) {
                                it.setInitialDelay(5L, TimeUnit.MINUTES)
                            } else {
                                it
                            }
                        }
                        .build()
                )
        }
    }

}