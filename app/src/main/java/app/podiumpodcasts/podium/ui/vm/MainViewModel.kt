package app.podiumpodcasts.podium.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    val db: AppDatabase,
    val defaultShowMediaPlayerBottomSheet: Boolean
) : ViewModel() {

    val hideFloatingMediaPlayer = mutableStateOf(false)

    val showMediaPlayerBottomSheet = mutableStateOf(false)

    init {
        viewModelScope.launch {
            delay(500)
            showMediaPlayerBottomSheet.value = defaultShowMediaPlayerBottomSheet
        }
    }

    fun fetchPodcast(
        origin: String
    ): Flow<PodcastModel> {
        return db.podcasts().get(origin)
    }

    fun fetchEpisode(
        id: String
    ): Flow<PodcastEpisodeBundle> {
        return db.podcastEpisodes().get(id)
    }

}