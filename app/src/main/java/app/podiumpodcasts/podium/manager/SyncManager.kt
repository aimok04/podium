package app.podiumpodcasts.podium.manager

import android.content.Context
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.sync.UnifiedSyncClient
import app.podiumpodcasts.podium.api.sync.UnifiedSyncClientType
import app.podiumpodcasts.podium.utils.getFriendlyDeviceName
import kotlinx.coroutines.flow.first
import java.util.UUID

class SyncManager {

    companion object {
        suspend fun createClient(
            settingsRepository: SettingsRepository
        ): UnifiedSyncClient {
            val type = settingsRepository.sync.type.first()

            return UnifiedSyncClient(
                type = when(type) {
                    "gpodder" -> UnifiedSyncClientType.GPODDER
                    "nextcloud" -> UnifiedSyncClientType.NEXTCLOUD_GPODDER
                    else -> throw Exception("invalid sync type")
                },

                deviceCaption = settingsRepository.sync.deviceCaption.first(),
                deviceId = settingsRepository.sync.deviceId.first(),

                baseUrl = settingsRepository.sync.baseUrl.first(),
                username = settingsRepository.sync.username.first(),
                password = settingsRepository.sync.password.first(),
                cookie = settingsRepository.sync.auth.first()
            )
        }

        fun generateDeviceId(deviceCaption: String): String {
            val allowedRegex = Regex("[^\\w.-]")

            val caption = deviceCaption
                .replace(" ", "-")
                .replace(allowedRegex, "")
                .trim('-', '.')
                .lowercase()

            val randomSuffix = UUID.randomUUID().toString()
                .replace("-", "")
                .take(5)

            return "$caption-$randomSuffix"
        }

        fun generateDeviceCaption(context: Context): String {
            return "podium on ${getFriendlyDeviceName(context)}"
        }
    }

}