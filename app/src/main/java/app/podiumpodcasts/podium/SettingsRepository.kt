package app.podiumpodcasts.podium

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val APPEARANCE_ENABLE_ARTWORK_COLORS = booleanPreferencesKey("appearance_enable_artwork_colors")
    val APPEARANCE_ENABLE_DYNAMIC_COLORS = booleanPreferencesKey("appearance_enable_dynamic_colors")
    val APPEARANCE_USE_ALTERNATIVE_BRANDING =
        booleanPreferencesKey("appearance_use_alternative_branding")

    val BEHAVIOR_UPDATE_PODCASTS_IN_ROAMING =
        booleanPreferencesKey("behavior_update_podcasts_in_roaming")
    val BEHAVIOR_UPDATE_PODCASTS_INTERVAL_MINUTES =
        intPreferencesKey("behavior_update_podcasts_interval_minutes")
    val BEHAVIOR_DOWNLOAD_METERED = booleanPreferencesKey("behavior_download_metered")
    val BEHAVIOR_DOWNLOAD_IN_ROAMING = booleanPreferencesKey("behavior_download_in_roaming")
    val BEHAVIOR_AUTO_DOWNLOADS_APPLY_SETTINGS =
        booleanPreferencesKey("behavior_auto_downloads_apply_settings")
    val BEHAVIOR_DELETE_PLAYED_DOWNLOADS = booleanPreferencesKey("behavior_delete_played_downloads")
    val BEHAVIOR_DELETE_DOWNLOADS_AFTER_SECONDS =
        intPreferencesKey("behavior_delete_downloads_after_seconds")

    val PRIVACY_DISABLE_APPLE_PODCASTS_API =
        booleanPreferencesKey("privacy_disable_apple_podcasts_api")

    val DEBUG_ENABLE_UPDATE_NOTIFICATION = booleanPreferencesKey("debug_enable_update_notification")
    val DEBUG_ENABLE_NIGHTLY_NOTIFICATION =
        booleanPreferencesKey("debug_enable_nightly_notification")
}

class SettingsRepository(val context: Context) {

    val dataStore = context.dataStore

    val appearance = Appearance()
    val behavior = Behavior()
    val privacy = Privacy()
    val debug = Debug()

    inner class Appearance {
        val enableArtworkColors: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.APPEARANCE_ENABLE_ARTWORK_COLORS] ?: true
            }

        suspend fun setEnableArtworkColors(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.APPEARANCE_ENABLE_ARTWORK_COLORS] = enable
        }

        val enableDynamicColors: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.APPEARANCE_ENABLE_DYNAMIC_COLORS] ?: true
            }

        suspend fun setEnableDynamicColors(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.APPEARANCE_ENABLE_DYNAMIC_COLORS] = enable
        }

        val useAlternativeBranding: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.APPEARANCE_USE_ALTERNATIVE_BRANDING] ?: false
            }

        suspend fun setUseAlternativeBranding(context: Context, enable: Boolean): Preferences {
            val default = ComponentName(context.packageName, "${context.packageName}.Default")
            val alias = ComponentName(context.packageName, "${context.packageName}.Alias")

            context.packageManager.setComponentEnabledSetting(
                if(enable) alias else default,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            context.packageManager.setComponentEnabledSetting(
                if(enable) default else alias,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            return dataStore.edit { preferences ->
                preferences[SettingsKeys.APPEARANCE_USE_ALTERNATIVE_BRANDING] = enable
            }
        }
    }

    inner class Behavior {
        val updatePodcastsInRoaming: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.BEHAVIOR_UPDATE_PODCASTS_IN_ROAMING] ?: false
            }

        suspend fun setUpdatePodcastsInRoaming(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_UPDATE_PODCASTS_IN_ROAMING] = enable
        }

        val updatePodcastsIntervalMinutes: Flow<Int> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.BEHAVIOR_UPDATE_PODCASTS_INTERVAL_MINUTES] ?: 60
            }

        suspend fun setUpdatePodcastsIntervalMinutes(minutes: Int) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_UPDATE_PODCASTS_INTERVAL_MINUTES] = minutes
        }

        val downloadMetered: Flow<Boolean> = dataStore.data
            .map { preferences -> preferences[SettingsKeys.BEHAVIOR_DOWNLOAD_METERED] ?: false }

        suspend fun setDownloadMetered(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_DOWNLOAD_METERED] = enable
        }

        val downloadInRoaming: Flow<Boolean> = dataStore.data
            .map { preferences -> preferences[SettingsKeys.BEHAVIOR_DOWNLOAD_IN_ROAMING] ?: false }

        suspend fun setDownloadInRoaming(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_DOWNLOAD_IN_ROAMING] = enable
        }

        val applySettingsForAutoDownloads: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.BEHAVIOR_AUTO_DOWNLOADS_APPLY_SETTINGS] ?: false
            }

        suspend fun setApplySettingsForAutoDownloads(enable: Boolean) =
            dataStore.edit { preferences ->
                preferences[SettingsKeys.BEHAVIOR_AUTO_DOWNLOADS_APPLY_SETTINGS] = enable
            }

        val deletePlayedDownloads: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.BEHAVIOR_DELETE_PLAYED_DOWNLOADS] ?: false
            }

        suspend fun setDeletePlayedDownloads(enabled: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_DELETE_PLAYED_DOWNLOADS] = enabled
        }

        val deleteDownloadsAfterSeconds: Flow<Int> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.BEHAVIOR_DELETE_DOWNLOADS_AFTER_SECONDS] ?: -1
            }

        suspend fun setDeleteDownloadsAfterSeconds(seconds: Int) = dataStore.edit { preferences ->
            preferences[SettingsKeys.BEHAVIOR_DELETE_DOWNLOADS_AFTER_SECONDS] = seconds
        }
    }

    inner class Privacy {
        val disableApplePodcastsApi: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.PRIVACY_DISABLE_APPLE_PODCASTS_API] ?: false
            }

        suspend fun setDisableApplePodcastsApi(disable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.PRIVACY_DISABLE_APPLE_PODCASTS_API] = disable
        }
    }

    inner class Debug {
        val enableUpdateNotification: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.DEBUG_ENABLE_UPDATE_NOTIFICATION] ?: false
            }

        suspend fun setEnableUpdateNotification(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.DEBUG_ENABLE_UPDATE_NOTIFICATION] = enable
        }

        val enableNightlyNotification: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.DEBUG_ENABLE_NIGHTLY_NOTIFICATION] ?: false
            }

        suspend fun setEnableNightlyNotification(enable: Boolean) = dataStore.edit { preferences ->
            preferences[SettingsKeys.DEBUG_ENABLE_NIGHTLY_NOTIFICATION] = enable
        }
    }
}