package app.podiumpodcasts.podium.ui.route.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import app.podiumpodcasts.podium.ui.DetailPaneKey
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.navigation.OpmlImporting
import app.podiumpodcasts.podium.ui.navigation.Restore
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsAppearanceKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsAppearancePane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsBackgroundActivityKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsBackgroundActivityPane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDatabaseKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDatabasePane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDebugKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDebugPane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDownloadsAndStorageKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsDownloadsAndStoragePane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPlaybackKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPlaybackPane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPrivacyKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsPrivacyPane
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsSynchronizationKey
import app.podiumpodcasts.podium.ui.route.settings.pane.SettingsSynchronizationPane
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
open class SettingsPaneKey : DetailPaneKey()

@Composable
fun SettingsPane(
    showBackButton: Boolean,

    contentKey: SettingsPaneKey,
    backStack: NavBackStack<NavKey>,

    onClose: () -> Unit
) {
    BackHandler {
        onClose()
    }

    @Composable
    fun NavigationIcon() {
        if(showBackButton) BackButton {
            onClose()
        }
    }

    when(contentKey) {
        is SettingsDatabaseKey -> {
            SettingsDatabasePane(
                navigationIcon = {
                    NavigationIcon()
                },

                onRestore = {
                    onClose()
                    backStack.add(Restore)
                },
                onOpmlImport = {
                    onClose()
                    backStack.add(OpmlImporting)
                }
            )
        }

        is SettingsAppearanceKey -> {
            SettingsAppearancePane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsPlaybackKey -> {
            SettingsPlaybackPane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsBackgroundActivityKey -> {
            SettingsBackgroundActivityPane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsDownloadsAndStorageKey -> {
            SettingsDownloadsAndStoragePane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsSynchronizationKey -> {
            SettingsSynchronizationPane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsPrivacyKey -> {
            SettingsPrivacyPane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        is SettingsDebugKey -> {
            SettingsDebugPane(
                navigationIcon = {
                    NavigationIcon()
                }
            )
        }

        else -> throw Exception("Unknown instance of SettingsPaneKey")
    }
}