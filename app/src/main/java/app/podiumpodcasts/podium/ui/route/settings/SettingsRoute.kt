package app.podiumpodcasts.podium.ui.route.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
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
import app.podiumpodcasts.podium.GITHUB_LINK
import app.podiumpodcasts.podium.KOFI_LINK
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.common.ExperimentalBadge
import app.podiumpodcasts.podium.ui.component.settings.SettingsHeader
import app.podiumpodcasts.podium.ui.component.settings.SettingsListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsAppearanceKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsBackgroundActivityKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDatabaseKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDebugKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDownloadsAndStorageKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPlaybackKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPrivacyKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsSynchronizationKey
import app.podiumpodcasts.podium.ui.vm.RoamingWarningDialogState
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsRoute(
    onLicenses: () -> Unit,
    onPane: (key: SettingsPaneKey) -> Unit,
    onBack: () -> Unit
) {
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
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.ColorLens,
                            stringResource(R.string.route_settings_appearance)
                        )
                    },

                    label = stringResource(R.string.route_settings_appearance),

                    index = 0,
                    count = 7,

                    onClick = {
                        onPane(SettingsAppearanceKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            stringResource(R.string.route_settings_playback)
                        )
                    },

                    label = stringResource(R.string.route_settings_playback),

                    index = 1,
                    count = 7,

                    onClick = {
                        onPane(SettingsPlaybackKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Storage,
                            stringResource(R.string.route_settings_database_and_backup)
                        )
                    },

                    label = stringResource(R.string.route_settings_database_and_backup),

                    index = 2,
                    count = 7,

                    onClick = {
                        onPane(SettingsDatabaseKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Construction,
                            stringResource(R.string.route_settings_background_activity)
                        )
                    },

                    label = stringResource(R.string.route_settings_background_activity),

                    index = 3,
                    count = 7,

                    onClick = {
                        onPane(SettingsBackgroundActivityKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Download,
                            stringResource(R.string.route_settings_downloads_and_storage)
                        )
                    },

                    label = stringResource(R.string.route_settings_downloads_and_storage),

                    index = 4,
                    count = 7,

                    onClick = {
                        onPane(SettingsDownloadsAndStorageKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Sync,
                            stringResource(R.string.route_settings_synchronization)
                        )
                    },

                    label = stringResource(R.string.route_settings_synchronization),

                    trailingContent = {
                        ExperimentalBadge()
                    },

                    index = 5,
                    count = 7,

                    onClick = {
                        onPane(SettingsSynchronizationKey())
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Visibility,
                            stringResource(R.string.route_settings_privacy)
                        )
                    },

                    label = stringResource(R.string.route_settings_privacy),

                    index = 6,
                    count = 7,

                    onClick = {
                        onPane(SettingsPrivacyKey())
                    }
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                SettingsListItem(
                    icon = {
                        Icon(Icons.Rounded.Work, "Debug")
                    },
                    label = "Debug",
                    onClick = {
                        onPane(SettingsDebugKey())
                    }
                )
            }
        }
    }
}

@Composable
fun RoamingWarningDialog(vm: SettingsViewModel) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = LocalDatabase.current

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