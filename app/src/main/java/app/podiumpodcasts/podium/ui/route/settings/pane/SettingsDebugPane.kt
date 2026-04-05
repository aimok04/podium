package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import app.podiumpodcasts.podium.background.work.DeletePlayedDownloadsWork
import app.podiumpodcasts.podium.background.work.FixSeedColorsWork
import app.podiumpodcasts.podium.background.worker.NightlyWorker
import app.podiumpodcasts.podium.background.worker.PeriodicPodcastUpdateWorker
import app.podiumpodcasts.podium.ui.component.settings.SettingsListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsDebugKey : SettingsPaneKey()

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsDebugPane(
    navigationIcon: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = LocalDatabase.current
    val settingsRepository = LocalSettingsRepository.current

    val vm = viewModel { SettingsViewModel(db, settingsRepository) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = navigationIcon,

                title = {
                    Text("Debug")
                }
            )
        }
    ) {
        LazyColumn(
            Modifier
                .padding(it)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item {
                val enableUpdateNotification =
                    vm.repository.debug.enableUpdateNotification.collectAsState(false)

                SettingsSwitchListItem(
                    checked = enableUpdateNotification.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.debug.setEnableUpdateNotification(it)
                        }
                    },

                    icon = {
                        Icon(Icons.Rounded.Notifications, "")
                    },
                    label = "Enable update notifications",
                    description = "Receive debug notifications when periodic podcast updates run",

                    index = 0,
                    count = 2
                )
            }

            item {
                val enableNightlyNotification =
                    vm.repository.debug.enableNightlyNotification.collectAsState(false)

                SettingsSwitchListItem(
                    checked = enableNightlyNotification.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.debug.setEnableNightlyNotification(it)
                        }
                    },

                    icon = {
                        Icon(Icons.Rounded.Notifications, "")
                    },
                    label = "Enable nightly notifications",
                    description = "Receive debug notifications when nightly tasks run",

                    index = 1,
                    count = 2
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.Update, "")
                    },
                    label = "Run podcast update worker",
                    description = "Fetch all podcast feeds for updates",

                    index = 0,
                    count = 3,

                    onClick = {
                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                uniqueWorkName = "PeriodicPodcastUpdateWorkerInstant",
                                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                                request = OneTimeWorkRequestBuilder<PeriodicPodcastUpdateWorker>()
                                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                    .build()
                            )
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.Bedtime, "")
                    },
                    label = "Run nightly worker",
                    description = "Run all nightly tasks",

                    index = 1,
                    count = 6,

                    onClick = {
                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                uniqueWorkName = "DailyWorker-Once",
                                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                                request = OneTimeWorkRequestBuilder<NightlyWorker>()
                                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                    .build()
                            )
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.CleaningServices, "")
                    },
                    label = "Delete latest episodes",
                    description = "Delete last episodes from each podcast",

                    index = 2,
                    count = 6,

                    onClick = {
                        scope.launch {
                            val podcasts = db.podcasts().allSync()

                            podcasts.forEach {
                                val episodes = db.podcastEpisodes().all(it.origin).first()

                                db.podcastEpisodes().delete(episodes.first().episode.id)
                                db.podcastSubscriptions()
                                    .storeCacheValues(it.origin, "", "", "")
                            }
                        }
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.Palette, "")
                    },
                    label = "Fix seed colors",
                    description = "Create seed colors for all podcasts",

                    index = 3,
                    count = 6,

                    onClick = {
                        scope.launch {
                            val fix = FixSeedColorsWork(context, db)
                            fix.doWork()
                        }
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.DeleteSweep, "")
                    },
                    label = "Delete all played downloads",
                    description = "Run delete played downloads work",

                    index = 4,
                    count = 6,

                    onClick = {
                        scope.launch {
                            val fix = DeletePlayedDownloadsWork(context, db)
                            fix.doWork()
                        }
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.AutoGraph, "")
                    },
                    label = "Delete update podcast statistics",
                    description = "This will reset the update podcast data usage estimate",

                    index = 5,
                    count = 6,

                    onClick = {
                        scope.launch {
                            db.statisticsUpdatePodcastRun().clear()
                        }
                    }
                )
            }
        }
    }
}