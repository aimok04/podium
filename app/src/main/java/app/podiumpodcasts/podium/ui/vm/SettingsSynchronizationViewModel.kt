package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.sync.gpodder.GpodderClient
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.NextcloudGpodderClient
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.model.PollResult
import app.podiumpodcasts.podium.background.worker.sync.FullSynchronizationWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    object Done : LoginState
    data class Failure(val message: String?) : LoginState
}

class SettingsSynchronizationViewModel(
    val db: AppDatabase,
    val repository: SettingsRepository
) : ViewModel() {

    val loginState = mutableStateOf<LoginState>(LoginState.Idle)

    fun gpodderLogin(
        context: Context,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            loginState.value = LoginState.Loading

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

                repository.sync.setType("gpodder")

                repository.sync.setUsername(username)
                repository.sync.setPassword(password)
                repository.sync.setAuth(result.result.cookie)

                repository.sync.setTimestampSubscriptions(0L)
                repository.sync.setTimestampEpisodeActions(0L)

                loginState.value = LoginState.Done

                FullSynchronizationWorker.enqueue(context)
            } catch(e: Exception) {
                loginState.value = LoginState.Failure(e.toString())
            }
        }
    }

    fun nextcloudLogin(
        context: Context
    ) {
        viewModelScope.launch {
            loginState.value = LoginState.Loading

            try {
                val client = NextcloudGpodderClient(
                    baseUrl = repository.sync.baseUrl.first()
                )

                val result = client.auth.startLogin()

                val intent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()

                intent.launchUrl(context, result.result.login.toUri())

                var attempts = 0
                while(attempts < 100) {
                    attempts++

                    try {
                        delay(1500)

                        val pollResult = client.auth.poll(result.result.poll)
                        if(pollResult.result is PollResult.Successful) {
                            repository.sync.setType("nextcloud")

                            repository.sync.setUsername(pollResult.result.loginName)
                            repository.sync.setPassword(pollResult.result.appPassword)
                            repository.sync.setAuth("logged in")

                            repository.sync.setTimestampSubscriptions(0L)
                            repository.sync.setTimestampEpisodeActions(0L)

                            loginState.value = LoginState.Done

                            FullSynchronizationWorker.enqueue(context)
                            break
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                loginState.value = LoginState.Failure(e.toString())
            }
        }
    }

    suspend fun resetAuth() {
        repository.sync.setUsername("")
        repository.sync.setPassword("")
        repository.sync.setAuth("")
    }

}