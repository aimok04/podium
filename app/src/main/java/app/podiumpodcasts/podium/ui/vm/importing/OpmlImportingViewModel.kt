package app.podiumpodcasts.podium.ui.vm.importing

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.opml.model.OpmlFile
import app.podiumpodcasts.podium.manager.PodcastManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface State {
    object SelectFile : State
    data class InvalidFile(val reason: String) : State
    data class SelectOutlines(val file: OpmlFile) : State
    object Unpacking : State
    object Done : State
}

class OpmlImportingViewModel : ViewModel() {

    val state = mutableStateOf<State>(State.SelectFile)

    val selectedOrigins = mutableStateSetOf<String>()
    val existingOrigins = mutableStateSetOf<String>()

    fun init() {
        state.value = State.SelectFile
    }

    fun selectFile(context: Context, db: AppDatabase, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val podcastOrigins = db.podcasts().allOrigins()

            try {
                val xml = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }

                val file = OpmlFile.parse(xml!!)

                existingOrigins.clear()
                file.body.outlines.forEach { outline ->
                    if(podcastOrigins.contains(outline.xmlUrl))
                        existingOrigins.add(outline.xmlUrl!!)
                }

                withContext(Dispatchers.Main) {
                    selectedOrigins.clear()
                    state.value = State.SelectOutlines(file)
                }
            } catch(e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    state.value = State.InvalidFile(e.toString())
                }
            }
        }
    }

    fun add(
        context: Context,
        podcastManager: PodcastManager
    ) {
        state.value = State.Unpacking

        viewModelScope.launch(Dispatchers.IO) {
            for(origin in selectedOrigins) {
                podcastManager.addPodcast(
                    origin = origin,
                    seedColor = null
                )
            }

            withContext(Dispatchers.Main) {
                state.value = State.Done
            }
        }
    }

}