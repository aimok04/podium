package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsAppearanceKey : SettingsPaneKey()

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