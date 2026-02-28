package app.podiumpodcasts.podium.ui.route.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExploreOff
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import app.podiumpodcasts.podium.GITHUB_LINK
import app.podiumpodcasts.podium.KOFI_LINK
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.background.work.DeletePlayedDownloadsWork
import app.podiumpodcasts.podium.background.work.FixSeedColorsWork
import app.podiumpodcasts.podium.background.worker.NightlyWorker
import app.podiumpodcasts.podium.background.worker.PeriodicPodcastUpdateWorker
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.settings.SettingsHeader
import app.podiumpodcasts.podium.ui.component.settings.SettingsListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSliderListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.formatFileSize
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.vm.DeleteDownloadsAfterValues
import app.podiumpodcasts.podium.ui.vm.ExportDatabaseState
import app.podiumpodcasts.podium.ui.vm.RoamingWarningDialogState
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import app.podiumpodcasts.podium.ui.vm.UPDATE_PODCASTS_INTERVAL_VALUES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsRoute(
    onLicenses: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = LocalDatabase.current
    val settingsRepository = LocalSettingsRepository.current

    val uriHandler = LocalUriHandler.current

    val vm = viewModel { SettingsViewModel(db, settingsRepository) }

    val updatePodcastsIntervalMinutesState =
        vm.repository.behavior.updatePodcastsIntervalMinutes.collectAsState(60)
    LaunchedEffect(updatePodcastsIntervalMinutesState.value) {
        vm.updateUpdatePodcastsIntervalMinutesSlider(
            updatePodcastsIntervalMinutesState.value
        )
    }

    val deleteDownloadsAfterSecondsState =
        vm.repository.behavior.deleteDownloadsAfterSeconds.collectAsState(-1)
    LaunchedEffect(deleteDownloadsAfterSecondsState.value) {
        vm.updateDeleteDownloadsAfterSlider(
            deleteDownloadsAfterSecondsState.value
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton {
                        onBack()
                    }
                },
                title = {
                    Text(stringResource(R.string.route_settings))
                }
            )
        }
    ) { inset ->
        val downloadMetered = vm.repository.behavior.downloadMetered.collectAsState(false)

        LazyColumn(
            Modifier
                .padding(inset)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item {
                SettingsHeader(
                    label = stringResource(R.string.route_settings_about)
                )
            }

            item {
                val version = remember {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    Pair(packageInfo.versionName, packageInfo.longVersionCode)
                }

                SettingsListItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.ic_notification_icon),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = stringResource(R.string.app_name),
                    description = "v${version.first} (${version.second})",

                    index = 0,
                    count = 3,

                    onClick = {
                        uriHandler.openUri(GITHUB_LINK)
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Balance,
                            contentDescription = ""
                        )
                    },
                    label = "Open-source licenses",
                    description = "See used software and their licenses",

                    index = 1,
                    count = 3,

                    onClick = {
                        onLicenses()
                    }
                )
            }


            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.VolunteerActivism,
                            contentDescription = ""
                        )
                    },
                    label = "Support the development on Ko-Fi",
                    description = "You could buy me a coffee over there :)",

                    index = 2,
                    count = 3,

                    onClick = {
                        uriHandler.openUri(KOFI_LINK)
                    }
                )
            }

            item {
                Spacer(
                    Modifier.height(32.dp)
                )
            }

            item {
                SettingsHeader(
                    label = stringResource(R.string.route_settings_database)
                )
            }

            item {
                val size = remember { vm.calculateDatabaseSize(context) ?: "" }

                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.DataUsage,
                            stringResource(R.string.route_settings_database_storage_used)
                        )
                    },
                    label = stringResource(R.string.route_settings_database_storage_used),
                    description = size,
                    index = 0,
                    count = 3
                ) {

                }
            }

            item {
                val state = vm.exportDatabaseState.value

                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Save,
                            stringResource(R.string.route_settings_database_export)
                        )
                    },
                    label = stringResource(R.string.route_settings_database_export),
                    description = when(state) {
                        is ExportDatabaseState.Writing -> "${state.fileName} ..."
                        else -> stringResource(R.string.route_settings_database_export_description)
                    },

                    index = 1,
                    count = 3,

                    onClick = {
                        vm.exportAndShareDatabase(
                            context = context,
                            title = context.getString(R.string.route_settings_database_export_share_title)
                        )
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Restore,
                            stringResource(R.string.route_settings_database_restore)
                        )
                    },
                    label = stringResource(R.string.route_settings_database_restore),
                    description = stringResource(
                        R.string.route_settings_database_restore_description,
                        stringResource(R.string.app_name)
                    ),

                    index = 2,
                    count = 3,

                    onClick = {
                        onRestore()
                    }
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsHeader(
                    label = stringResource(R.string.route_settings_appearance)
                )
            }

            item {
                val enableArtworkColors =
                    vm.repository.appearance.enableArtworkColors.collectAsState(true)

                SettingsSwitchListItem(
                    checked = enableArtworkColors.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.appearance.setEnableArtworkColors(it)
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.Colorize,
                            stringResource(R.string.route_settings_appearance_enable_artwork_colors)
                        )
                    },
                    label = stringResource(R.string.route_settings_appearance_enable_artwork_colors),
                    description = stringResource(R.string.route_settings_appearance_enable_artwork_colors_description),

                    index = 0,
                    count = 2
                )
            }

            item {
                val useAlternativeBranding =
                    vm.repository.appearance.useAlternativeBranding.collectAsState(true)

                SettingsSwitchListItem(
                    checked = useAlternativeBranding.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.appearance.setUseAlternativeBranding(context, it)
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.AppRegistration,
                            stringResource(R.string.route_settings_appearance_use_alternative_branding)
                        )
                    },
                    label = stringResource(R.string.route_settings_appearance_use_alternative_branding),
                    description = stringResource(R.string.route_settings_appearance_use_alternative_branding_description),

                    index = 1,
                    count = 2
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsHeader(
                    label = stringResource(R.string.route_settings_background_activity)
                )
            }

            item {
                val updatePodcastsInRoaming =
                    vm.repository.behavior.updatePodcastsInRoaming.collectAsState(false)

                SettingsSwitchListItem(
                    checked = updatePodcastsInRoaming.value,
                    onCheckedChange = {
                        if(it) {
                            vm.roamingWarningDialogState.value =
                                RoamingWarningDialogState.ShowUpdate
                        } else {
                            scope.launch {
                                vm.repository.behavior.setUpdatePodcastsInRoaming(false)
                                vm.requeueUpdates(context)
                            }
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.Public,
                            stringResource(R.string.route_settings_background_activity_allow_update_while_roaming)
                        )
                    },
                    label = stringResource(R.string.route_settings_background_activity_allow_update_while_roaming),
                    description = stringResource(R.string.route_settings_background_activity_allow_update_while_roaming_description),

                    index = 0,
                    count = 2
                )
            }

            item {
                SettingsSliderListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.SignalCellularAlt,
                            stringResource(R.string.route_settings_background_activity_update_frequency)
                        )
                    },
                    label = stringResource(R.string.route_settings_background_activity_update_frequency),

                    value = vm.updatePodcastsIntervalMinutesSliderState.floatValue,
                    onValueChange = { vm.updateUpdatePodcastsIntervalMinutesSlider(it) },
                    valueRange = 0f..(UPDATE_PODCASTS_INTERVAL_VALUES.size - 1).toFloat(),
                    steps = UPDATE_PODCASTS_INTERVAL_VALUES.size - 1,

                    onValueChangeFinished = {
                        scope.launch {
                            vm.repository.behavior.setUpdatePodcastsIntervalMinutes(
                                vm.updatePodcastsIntervalMinutesTranslatedSliderState.intValue
                            )
                            vm.requeueUpdates(context)
                        }
                    },

                    supportingContent = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            vm.updatePodcastsIntervalMinutesTranslatedSliderState.intValue.let { state ->
                                val hours = state / 60
                                val minutes = state % 60

                                Text(
                                    text = if(hours > 0)
                                        if(minutes > 0)
                                            stringResource(
                                                R.string.route_settings_background_activity_update_frequency_every_hours_mins,
                                                hours,
                                                minutes
                                            )
                                        else
                                            stringResource(
                                                R.string.route_settings_background_activity_update_frequency_every_hours,
                                                hours
                                            )
                                    else
                                        stringResource(
                                            R.string.route_settings_background_activity_update_frequency_every_min,
                                            minutes
                                        )
                                )

                                vm.avgUpdateRunDataUsage.value?.let { avg ->
                                    val updatesPerMonth = (1440 * 30) / state

                                    Badge {
                                        Text(
                                            text = stringResource(
                                                R.string.route_settings_background_activity_update_frequency_usage,
                                                formatFileSize(updatesPerMonth.toLong() * avg)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },

                    index = 1,
                    count = 2
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsHeader(
                    label = stringResource(R.string.route_settings_downloads_and_storage)
                )
            }

            item {
                SettingsSwitchListItem(
                    checked = downloadMetered.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.behavior.setDownloadMetered(it)
                            vm.requeueDownloads(context, db)
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.SignalCellularAlt,
                            stringResource(R.string.route_settings_downloads_and_storage_allow_mobile_downloads)
                        )
                    },
                    label = stringResource(R.string.route_settings_downloads_and_storage_allow_mobile_downloads),
                    description = stringResource(R.string.route_settings_downloads_and_storage_allow_mobile_downloads_description),

                    index = 0,
                    count = 3
                )
            }

            item {
                val downloadInRoaming =
                    vm.repository.behavior.downloadInRoaming.collectAsState(false)

                SettingsSwitchListItem(
                    enabled = downloadMetered.value,

                    checked = downloadInRoaming.value,
                    onCheckedChange = {
                        if(it) {
                            vm.roamingWarningDialogState.value =
                                RoamingWarningDialogState.ShowDownload
                        } else {
                            scope.launch {
                                vm.repository.behavior.setDownloadInRoaming(false)
                                vm.requeueDownloads(context, db)
                            }
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.Public,
                            stringResource(R.string.route_settings_downloads_and_storage_allow_while_roaming)
                        )
                    },
                    label = stringResource(R.string.route_settings_downloads_and_storage_allow_while_roaming),
                    description = stringResource(R.string.route_settings_downloads_and_storage_allow_while_roaming_description),
                    index = 1,
                    count = 3
                )
            }

            item {
                val applySettingsForAutoDownloads =
                    vm.repository.behavior.applySettingsForAutoDownloads.collectAsState(false)

                SettingsSwitchListItem(
                    enabled = downloadMetered.value,

                    checked = applySettingsForAutoDownloads.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.behavior.setApplySettingsForAutoDownloads(it)
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.FileDownload,
                            stringResource(R.string.route_settings_downloads_and_storage_include_automatic_downloads)
                        )
                    },
                    label = stringResource(R.string.route_settings_downloads_and_storage_include_automatic_downloads),
                    description = stringResource(R.string.route_settings_downloads_and_storage_include_automatic_downloads_description),

                    index = 2,
                    count = 3
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                val deletePlayedDownloads =
                    vm.repository.behavior.deletePlayedDownloads.collectAsState(false)

                SettingsSwitchListItem(
                    checked = deletePlayedDownloads.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.behavior.setDeletePlayedDownloads(it)
                        }
                    },

                    icon = {
                        Icon(
                            Icons.Rounded.CleaningServices,
                            stringResource(R.string.route_settings_downloads_and_storage_delete_played_downloads)
                        )
                    },
                    label = stringResource(R.string.route_settings_downloads_and_storage_delete_played_downloads),
                    description = stringResource(R.string.route_settings_downloads_and_storage_delete_played_downloads_description),

                    index = 0,
                    count = 2
                )
            }

            item {
                SettingsSliderListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.AutoDelete,
                            stringResource(R.string.route_settings_downloads_and_storage_delete_downloads_after)
                        )
                    },
                    label = stringResource(R.string.route_settings_downloads_and_storage_delete_downloads_after),

                    value = vm.deleteDownloadsAfterSliderState.floatValue,
                    onValueChange = { vm.updateDeleteDownloadsAfterSlider(it) },
                    valueRange = 0f..(DeleteDownloadsAfterValues.entries.size - 1).toFloat(),
                    steps = DeleteDownloadsAfterValues.entries.size - 2,

                    onValueChangeFinished = {
                        scope.launch {
                            vm.repository.behavior.setDeleteDownloadsAfterSeconds(
                                vm.deleteDownloadsAfterTranslatedSliderState.value.seconds
                            )
                        }
                    },

                    supportingContent = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            vm.deleteDownloadsAfterTranslatedSliderState.value.let { state ->
                                Text(
                                    text = stringResource(state.label)
                                )
                            }
                        }
                    },

                    index = 1,
                    count = 2
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsHeader(
                    stringResource(R.string.route_settings_privacy)
                )
            }

            item {
                val disableApplePodcastsApi =
                    vm.repository.privacy.disableApplePodcastsApi.collectAsState(false)

                SettingsSwitchListItem(
                    checked = disableApplePodcastsApi.value,
                    onCheckedChange = {
                        scope.launch {
                            vm.repository.privacy.setDisableApplePodcastsApi(it)
                        }
                    },

                    icon = {
                        Icon(Icons.Rounded.ExploreOff, "")
                    },
                    label = stringResource(R.string.route_settings_privacy_disable_apple_podcasts_api),
                    description = stringResource(R.string.route_settings_privacy_disable_apple_podcasts_api_description),

                    index = 0,
                    count = 1
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsHeader(
                    "Debug"
                )
            }

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

    if(vm.roamingWarningDialogState.value != RoamingWarningDialogState.Hide) AlertDialog(
        onDismissRequest = {
            vm.roamingWarningDialogState.value = RoamingWarningDialogState.Hide
        },

        icon = {
            Icon(
                Icons.Rounded.Warning,
                stringResource(R.string.route_settings_roaming_warning_title)
            )
        },
        title = {
            Text(stringResource(R.string.route_settings_roaming_warning_title))
        },
        text = {
            Text(stringResource(R.string.route_settings_roaming_warning_description))
        },

        containerColor = MaterialTheme.colorScheme.errorContainer,
        iconContentColor = MaterialTheme.colorScheme.error,
        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer,

        dismissButton = {
            TextButton(
                onClick = {
                    vm.roamingWarningDialogState.value = RoamingWarningDialogState.Hide
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.common_action_abort))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        when(vm.roamingWarningDialogState.value) {
                            is RoamingWarningDialogState.ShowUpdate -> {
                                vm.repository.behavior.setUpdatePodcastsInRoaming(true)
                                vm.requeueUpdates(context)
                            }

                            is RoamingWarningDialogState.ShowDownload -> {
                                vm.repository.behavior.setDownloadInRoaming(true)
                                vm.requeueDownloads(context, db)
                            }
                        }

                        vm.roamingWarningDialogState.value = RoamingWarningDialogState.Hide
                    }
                },

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.common_action_continue))
            }
        }
    )
}