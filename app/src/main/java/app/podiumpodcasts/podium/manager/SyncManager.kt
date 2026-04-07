package app.podiumpodcasts.podium.manager

import android.content.Context
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.gpodder.GpodderClient
import app.podiumpodcasts.podium.utils.getFriendlyDeviceName
import kotlinx.coroutines.flow.first
import java.util.UUID

class SyncManager {

    companion object {
        suspend fun createGpodderClient(
            settingsRepository: SettingsRepository,
            username: String? = null,
            password: String? = null
        ): GpodderClient {
            return GpodderClient(
                deviceCaption = settingsRepository.sync.deviceCaption.first(),
                deviceId = settingsRepository.sync.deviceId.first(),
                baseUrl = settingsRepository.sync.baseUrl.first(),
                username = username ?: settingsRepository.sync.username.first(),
                password = password ?: settingsRepository.sync.password.first(),
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