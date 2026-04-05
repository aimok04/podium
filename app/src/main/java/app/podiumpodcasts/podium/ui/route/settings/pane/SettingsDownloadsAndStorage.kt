package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SignalCellularAlt
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.settings.SettingsSliderListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.RoamingWarningDialog
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.vm.DeleteDownloadsAfterValues
import app.podiumpodcasts.podium.ui.vm.RoamingWarningDialogState
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsDownloadsAndStorageKey : SettingsPaneKey()

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsDownloadsAndStoragePane(
    navigationIcon: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val db = LocalDatabase.current
    val settingsRepository = LocalSettingsRepository.current

    val vm = viewModel { SettingsViewModel(db, settingsRepository) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = navigationIcon,

                title = {
                    Text(stringResource(R.string.route_settings_downloads_and_storage))
                }
            )
        }
    ) {
        val downloadMetered = vm.repository.behavior.downloadMetered.collectAsState(false)

        LazyColumn(
            Modifier
                .padding(it)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
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
        }
    }

    RoamingWarningDialog(vm)
}