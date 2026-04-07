package app.podiumpodcasts.podium.background.worker.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.model.SyncActionType
import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.manager.DatabaseManager
import app.podiumpodcasts.podium.manager.PodcastManager
import app.podiumpodcasts.podium.manager.SyncManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PartialSynchronizationWorker(
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
            Log.d("PartialSynchronizationWorker", "Sync is disabled. Returning.")
            return Result.success()
        }

        Log.d("PartialSynchronizationWorker", "Running partial sync ...")

        val client = SyncManager.createClient(settingsRepository)

        return try {
            val deviceId = settingsRepository.sync.deviceId.first()

            val currentTimestamp = System.currentTimeMillis() / 1000L

            val addSubscriptions = mutableListOf<String>()
            val removeSubscriptions = mutableListOf<String>()

            val episodeActions = mutableListOf<EpisodeAction>()

            db.syncActions().allBefore(currentTimestamp).forEach {
                when(it.actionType) {
                    SyncActionType.SUBSCRIBE.name -> {
                        addSubscriptions.add(it.origin)
                    }

                    SyncActionType.UNSUBSCRIBE.name -> {
                        removeSubscriptions.add(it.origin)
                    }

                    SyncActionType.PLAY.name -> {
                        episodeActions.add(
                            it.toEpisodeAction(
                                deviceId = deviceId
                            )
                        )
                    }
                }
            }

            if(addSubscriptions.isNotEmpty() || removeSubscriptions.isNotEmpty()) {
                client.subscriptions.uploadChanges(
                    add = addSubscriptions,
                    remove = removeSubscriptions
                )

                Log.d(
                    "PartialSynchronizationWorker",
                    "Uploaded ${addSubscriptions.size} subscription add's and ${removeSubscriptions.size} subscription remove's."
                )
            } else {
                Log.d(
                    "PartialSynchronizationWorker",
                    "No changes in subscriptions. Skipping upload."
                )
            }

            if(episodeActions.isNotEmpty()) {
                client.episodeActions.upload(
                    actions = episodeActions
                )
                Log.d(
                    "PartialSynchronizationWorker",
                    "Uploaded ${episodeActions.size} local episode actions."
                )
            } else {
                Log.d(
                    "PartialSynchronizationWorker",
                    "No local episode actions found. Skipping upload."
                )
            }

            db.syncActions()
                .deleteBefore(currentTimestamp)

            val timestampSubscriptions = settingsRepository.sync.timestampSubscriptions.first()
            val timestampEpisodeActions = settingsRepository.sync.timestampEpisodeActions.first()

            val subscriptionsResult = client.subscriptions.getChanges(timestampSubscriptions)
            val episodeActionsResult = client.episodeActions.get(timestampEpisodeActions, true)

            Log.d(
                "PartialSynchronizationWorker",
                "Received ${subscriptionsResult.result.add.size + subscriptionsResult.result.remove.size} subscription changes and ${episodeActionsResult.result.actions.size} episode action changes. Processing ..."
            )

            subscriptionsResult.result.add.forEach { origin ->
                podcastManager.addPodcast(origin, null)
                if(db.podcastSubscriptions().getSync(origin) == null) {
                    db.podcastSubscriptions().subscribe(origin)

                    Log.d(
                        "PartialSynchronizationWorker",
                        "Subscribed $origin due to remote change."
                    )
                }
            }

            subscriptionsResult.result.remove.forEach { origin ->
                db.podcastSubscriptions()
                    .unsubscribe(origin)

                Log.d("PartialSynchronizationWorker", "Unsubscribed $origin due to remote change.")
            }

            episodeActionsResult.result.actions.forEach { action ->
                if(action.type == "play") {
                    val episode = db.podcastEpisodes()
                        .getSyncByOriginAndAudioUrl(action.podcastOrigin, action.episodeAudioUrl)
                    if(episode == null) {
                        Log.d(
                            "PartialSynchronizationWorker",
                            "Episode action for episode ${action.podcastOrigin}:${action.episodeAudioUrl} could not be processed: not found."
                        )
                        return@forEach
                    }

                    if(action.timestampUnix <= (episode.playState?.lastUpdate ?: 0L)) {
                        Log.d(
                            "PartialSynchronizationWorker",
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
                        "PartialSynchronizationWorker",
                        "Synced progress for ${episode.episode.id}: ${action.position} / ${action.total} seconds"
                    )
                }
            }

            settingsRepository.sync.setTimestampSubscriptions(subscriptionsResult.result.timestamp)
            settingsRepository.sync.setTimestampEpisodeActions(episodeActionsResult.result.timestamp)

            Log.d("PartialSynchronizationWorker", "Finished partial sync.")
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
                Log.e("PartialSynchronizationWorker", "Error while trying to reauthenticate:")

                e.printStackTrace()
                Result.failure()
            }
        } catch(e: Exception) {
            Log.e("PartialSynchronizationWorker", "Error while trying to run partial sync:")

            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun enqueue(
            context: Context,
            debounceSeconds: Long = 0L
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    uniqueWorkName = "PartialSynchronizationWorker",
                    existingWorkPolicy = when(debounceSeconds) {
                        0L -> ExistingWorkPolicy.KEEP
                        else -> ExistingWorkPolicy.REPLACE
                    },
                    request = OneTimeWorkRequestBuilder<PartialSynchronizationWorker>()
                        .setInitialDelay(debounceSeconds, TimeUnit.SECONDS)
                        .setConstraints(
                            Constraints(
                                requiredNetworkType = NetworkType.NOT_ROAMING
                            )
                        ).build()
                )
        }

        fun schedule(
            context: Context,
            repeatIntervalMinutes: Long = 15L
        ) {
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    uniqueWorkName = "PeriodicSynchronizationWorker",
                    existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                    request = PeriodicWorkRequestBuilder<PartialSynchronizationWorker>(
                        repeatIntervalMinutes,
                        TimeUnit.MINUTES
                    )
                        .setInitialDelay(10L, TimeUnit.MINUTES)
                        .setConstraints(
                            Constraints(
                                requiredNetworkType = NetworkType.NOT_ROAMING,
                                requiresBatteryNotLow = true
                            )
                        ).build()
                )
        }
    }
}