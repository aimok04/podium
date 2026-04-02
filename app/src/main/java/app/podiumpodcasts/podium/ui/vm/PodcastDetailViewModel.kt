package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.background.work.SingularPodcastUpdateWork
import app.podiumpodcasts.podium.ui.view.model.Destinations
import coil3.Image
import coil3.asDrawable
import com.materialkolor.ktx.themeColorOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PodcastDetailViewModel(
    val db: AppDatabase,
    val podcast: PodcastModel
) : ViewModel() {

    var selectedDestination by mutableStateOf(Destinations.EPISODES)

    var isRefreshing by mutableStateOf(false)

    val showSettingsBottomSheet = mutableStateOf(false)
    val showDeleteDialog = mutableStateOf(false)

    val episodePager = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.podcastEpisodes()
            .allPaged(podcast.origin)
    }.flow.cachedIn(viewModelScope)

    val subscription =
        db.podcastSubscriptions().get(podcast.origin)

    val lazyListState = LazyListState()

    fun updatePodcast(context: Context, podcast: PodcastModel) {
        viewModelScope.launch {
            SingularPodcastUpdateWork(context, db)
                .doWork(podcast)
        }
    }

    fun enableNotifications() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .enableNotifications(podcast.origin)
        }
    }

    fun disableNotifications() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .disableNotifications(podcast.origin)
        }
    }

    fun enableAutoDownload() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .enableAutoDownload(podcast.origin)
        }
    }

    fun disableAutoDownload() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .disableAutoDownload(podcast.origin)
        }
    }

    fun subscribe() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .subscribe(podcast.origin)
        }
    }

    fun unsubscribe() {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .unsubscribe(podcast.origin)
        }
    }

    fun deletePodcast() {
        viewModelScope.launch {
            db.podcasts().delete(podcast)
        }
    }

    fun markAsPlayed(episode: PodcastEpisodeBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .savePlayed(episode.episode.id, true)
        }
    }

    fun updateImageSeedColor(
        context: Context,
        image: Image
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val themeColor = image.asDrawable(context.resources)
                .toBitmap().asImageBitmap().themeColorOrNull()

            db.podcasts().updateImageSeedColor(podcast.origin, themeColor?.toArgb() ?: -1)
        }
    }

}