package app.podiumpodcasts.podium.ui.vm

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.manager.AddPodcastResult
import app.podiumpodcasts.podium.manager.PodcastManager
import coil3.compose.AsyncImagePainter
import coil3.toBitmap
import com.materialkolor.ktx.themeColorOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PodcastPreviewState {
    open class Idle() : PodcastPreviewState
    open class Loading() : PodcastPreviewState
    open class Done(
        val podcast: PodcastModel
    ) : PodcastPreviewState

    open class Error(
        val reason: String
    ) : PodcastPreviewState
}

class PodcastPreviewViewModel(
    val db: AppDatabase,
    val podcastPreview: PodcastPreviewModel
) : ViewModel() {

    val podcastManager = PodcastManager(db)

    val listState = LazyListState()

    var state by mutableStateOf<PodcastPreviewState>(PodcastPreviewState.Idle())

    var duplicate by mutableStateOf<PodcastModel?>(null)

    var seedColor by mutableStateOf<Color?>(null)

    var showIgnoreSeedColorDialog by mutableStateOf(false)

    init {
        viewModelScope.launch {
            duplicate = db.podcasts().getSync(
                origin = podcastPreview.fetchUrl
            )
        }
    }

    fun add(
        force: Boolean = false
    ) {
        if(!force && seedColor == null) {
            showIgnoreSeedColorDialog = true
            return
        }

        viewModelScope.launch {
            state = PodcastPreviewState.Loading()

            try {
                val result = podcastManager.addPodcast(
                    origin = podcastPreview.fetchUrl,
                    seedColor = seedColor
                )

                when(result) {
                    is AddPodcastResult.Duplicate ->
                        duplicate = result.duplicate

                    is AddPodcastResult.Created ->
                        state = PodcastPreviewState.Done(result.podcast)
                }
            } catch(e: Exception) {
                e.printStackTrace()
                state = PodcastPreviewState.Error(e.toString())
            }
        }
    }

    fun extractSeedColor(state: AsyncImagePainter.State.Success) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageBitmap = state.result.image
                .toBitmap()
                .asImageBitmap()

            seedColor = imageBitmap.themeColorOrNull()
        }
    }

}