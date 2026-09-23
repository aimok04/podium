package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.settings.SettingsHeader
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.theme.ThemeMode
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsAppearanceKey : SettingsPaneKey()

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearancePane(
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
                    Text(stringResource(R.string.route_settings_appearance))
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
                SettingsHeader(
                    label = stringResource(R.string.route_settings_appearance_theme)
                )
            }

            item {
                val themeMode =
                    vm.repository.appearance.themeMode.collectAsState(ThemeMode.SYSTEM)

                Column(
                    modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ThemeMode.entries.size
                                ),
                                selected = mode == themeMode.value,
                                onClick = {
                                    scope.launch {
                                        vm.repository.appearance.setThemeMode(mode)
                                    }
                                    val uiModeManager =
                                        context.applicationContext.getSystemService(Context.UI_MODE_SERVICE)
                                            as UiModeManager
                                    uiModeManager.setApplicationNightMode(
                                        when (mode) {
                                            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
                                            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                                            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                                        }
                                    )
                                },
                                label = { Text(stringResource(themeModeLabel(mode))) },
                            )
                        }
                    }
                }
            }

            item {
                Spacer(
                    Modifier.height(32.dp)
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
        }
    }
}

private fun themeModeLabel(mode: ThemeMode) = when (mode) {
    ThemeMode.SYSTEM -> R.string.route_settings_appearance_theme_system
    ThemeMode.LIGHT -> R.string.route_settings_appearance_theme_light
    ThemeMode.DARK -> R.string.route_settings_appearance_theme_dark
}
