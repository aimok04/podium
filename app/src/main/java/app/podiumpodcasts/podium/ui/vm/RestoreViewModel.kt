package app.podiumpodcasts.podium.ui.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.window.embedding.ActivityFilter
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.manager.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface State {
    object SelectFile : State
    object InvalidFile : State
    data class OverwriteWarning(val uri: Uri) : State
    object Unpacking : State
    data class Error(val reason: String) : State
    object Restart : State
}

class RestoreViewModel : ViewModel() {

    val state = mutableStateOf<State>(State.SelectFile)

    fun selectFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isValid = DatabaseManager.isRestoreFileValid(context, uri)

                withContext(Dispatchers.Main) {
                    state.value = when(isValid) {
                        true -> State.OverwriteWarning(uri)
                        false -> State.InvalidFile
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    state.value = State.Error(e.toString())
                }
            }
        }
    }

    fun restore(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    state.value = State.Unpacking
                }

                DatabaseManager.restoreFromBackup(context, uri)
                delay(500)

                withContext(Dispatchers.Main) {
                    state.value = State.Restart
                }
            } catch(e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    state.value = State.Error(e.toString())
                }
            }
        }
    }

    fun restartApp(activity: Activity?) {
        activity?.let { activity ->
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent?.component)
            activity.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }

}