package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Badge
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
import app.podiumpodcasts.podium.ui.formatFileSize
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.RoamingWarningDialog
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.vm.RoamingWarningDialogState
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import app.podiumpodcasts.podium.ui.vm.UPDATE_PODCASTS_INTERVAL_VALUES
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsBackgroundActivityKey : SettingsPaneKey()

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsBackgroundActivityPane(
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
                    Text(stringResource(R.string.route_settings_background_activity))
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
        }
    }

    RoamingWarningDialog(vm)
}