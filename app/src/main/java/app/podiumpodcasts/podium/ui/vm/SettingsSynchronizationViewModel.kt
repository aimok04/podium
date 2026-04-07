package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.gpodder.GpodderClient
import app.podiumpodcasts.podium.background.worker.sync.FullSynchronizationWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface GpodderLoginState {
    object Idle : GpodderLoginState
    object Loading : GpodderLoginState
    object Done : GpodderLoginState
    data class Failure(val message: String?) : GpodderLoginState
}

class SettingsSynchronizationViewModel(
    val db: AppDatabase,
    val repository: SettingsRepository
) : ViewModel() {

    val gpodderLoginState = mutableStateOf<GpodderLoginState>(GpodderLoginState.Idle)

    fun gpodderLogin(
        context: Context,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            gpodderLoginState.value = GpodderLoginState.Loading

            try {
                val client = GpodderClient(
                    deviceCaption = repository.sync.deviceCaption.first(),
                    deviceId = repository.sync.deviceId.first(),

                    baseUrl = repository.sync.baseUrl.first(),
                    username = username,
                    password = password,
                    cookie = repository.sync.auth.first()
                )

                val result = client.auth.login()

                client.device.update()

                repository.sync.setUsername(username)
                repository.sync.setPassword(password)
                repository.sync.setAuth(result.result.cookie)

                repository.sync.setTimestampSubscriptions(0L)
                repository.sync.setTimestampEpisodeActions(0L)

                gpodderLoginState.value = GpodderLoginState.Done

                FullSynchronizationWorker.enqueue(context)
            } catch(e: Exception) {
                gpodderLoginState.value = GpodderLoginState.Failure(e.toString())
            }
        }
    }

    suspend fun resetAuth() {
        repository.sync.setUsername("")
        repository.sync.setPassword("")
        repository.sync.setAuth("")
    }

    fun setType(type: String) {
        viewModelScope.launch {
            repository.sync.setBaseUrl(
                when(type) {
                    "gpodder" -> "https://gpodder.net"
                    else -> ""
                }
            )

            resetAuth()

            repository.sync.setType(type)
        }
    }

}