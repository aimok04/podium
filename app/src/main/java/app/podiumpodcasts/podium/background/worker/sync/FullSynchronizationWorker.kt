package app.podiumpodcasts.podium.background.worker.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.manager.DatabaseManager
import app.podiumpodcasts.podium.manager.PodcastManager
import app.podiumpodcasts.podium.manager.SyncManager
import kotlinx.coroutines.flow.first

class FullSynchronizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(
    context, params
) {
    val db by lazy { DatabaseManager.build(context) }
    val settingsRepository: SettingsRepository = SettingsRepository(context)

    val podcastManager = PodcastManager(db)

    override suspend fun doWork(): Result {
        val isEnabled = settingsRepository.sync.enable.first()
        if(!isEnabled) {
            Log.d("FullSynchronizationWorker", "Sync is disabled. Returning.")
            return Result.success()
        }

        val client = SyncManager.createClient(settingsRepository)

        return try {
            val deviceId = settingsRepository.sync.deviceId.first()

            val subscriptionsResult = client.subscriptions.getChanges(0L)

            val remoteOrigins = subscriptionsResult.result.add
            val localOrigins = db.podcastSubscriptions().allOrigins().toSet()

            val newOrigins = remoteOrigins.filterNot { localOrigins.contains(it) }
            newOrigins.forEach { origin ->
                podcastManager.addPodcast(origin, null)
                if(db.podcastSubscriptions().getSync(origin) == null) {
                    db.podcastSubscriptions().subscribe(origin)
                    Log.d("FullSynchronizationWorker", "Subscribed $origin due to remote change.")
                }
            }

            val episodeActionsResult = client.episodeActions.get(0L, true)
            episodeActionsResult.result.actions.forEach { action ->
                if(action.type == "play") {
                    val episode = db.podcastEpisodes()
                        .getSyncByOriginAndAudioUrl(action.podcastOrigin, action.episodeAudioUrl)
                    if(episode == null) {
                        Log.d(
                            "FullSynchronizationWorker",
                            "Episode action for episode ${action.podcastOrigin}:${action.episodeAudioUrl} could not be processed: not found."
                        )
                        return@forEach
                    }

                    if(action.timestampUnix <= (episode.playState?.lastUpdate ?: 0L)) {
                        Log.d(
                            "FullSynchronizationWorker",
                            "Episode action for episode ${action.podcastOrigin}:${action.episodeAudioUrl} is outdated, skipping."
                        )
                        return@forEach
                    }

                    db.podcastEpisodePlayStates().set(
                        episodeId = episode.episode.id,
                        state = action.position ?: 0,
                        played = action.total == action.position,
                        lastUpdate = action.timestampUnix
                    )

                    Log.d(
                        "FullSynchronizationWorker",
                        "Synced progress for ${episode.episode.id}: ${action.position} / ${action.total} seconds"
                    )
                }
            }

            settingsRepository.sync.setTimestampSubscriptions(subscriptionsResult.result.timestamp)
            settingsRepository.sync.setTimestampEpisodeActions(episodeActionsResult.result.timestamp)

            client.subscriptions.upload(db.podcastSubscriptions().allOrigins())
            client.episodeActions.upload(
                db.podcastEpisodePlayStates().allSync().map {
                    it.toEpisodeAction(
                        deviceId = deviceId
                    )
                }
            )

            Result.success()
        } catch(e: SyncResult.Unauthenticated) {
            e.printStackTrace()

            try {
                val response = client.auth.relogin()
                if(response is SyncResult.Success) {
                    settingsRepository.sync.setAuth(response.result.cookie)
                    Result.retry()
                } else {
                    Result.failure()
                }
            } catch(e: Exception) {
                Log.e("FullSynchronizationWorker", "Error while trying to reauthenticate:")

                e.printStackTrace()
                Result.failure()
            }
        } catch(e: Exception) {
            Log.e("FullSynchronizationWorker", "Error while trying to run full sync:")

            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun enqueue(
            context: Context
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    uniqueWorkName = "FullSynchronizationWorker",
                    existingWorkPolicy = ExistingWorkPolicy.KEEP,
                    request = OneTimeWorkRequestBuilder<FullSynchronizationWorker>()
                        .setConstraints(
                            Constraints(
                                requiredNetworkType = NetworkType.NOT_ROAMING
                            )
                        ).build()
                )
        }
    }
}