package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.background.work.SingularPodcastUpdateWork
import app.podiumpodcasts.podium.ui.view.model.Destinations
import kotlinx.coroutines.launch

class PodcastDetailViewModel : ViewModel() {

    var selectedDestination by mutableStateOf(Destinations.EPISODES)

    var isRefreshing by mutableStateOf(false)


    val showSettingsBottomSheet = mutableStateOf(false)
    val showDeleteDialog = mutableStateOf(false)

    fun episodeBundleList(db: AppDatabase, podcast: PodcastModel) =
        db.podcastEpisodes().all(podcast.origin)

    fun subscription(db: AppDatabase, podcast: PodcastModel) =
        db.podcastSubscriptions().get(podcast.origin)

    val lazyListState = LazyListState()

    fun updatePodcast(db: AppDatabase, context: Context, podcast: PodcastModel) {
        viewModelScope.launch {
            SingularPodcastUpdateWork(context, db)
                .doWork(podcast)
        }
    }

    fun enableNotifications(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .enableNotifications(podcast.origin)
        }
    }

    fun disableNotifications(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .disableNotifications(podcast.origin)
        }
    }

    fun enableAutoDownload(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .enableAutoDownload(podcast.origin)
        }
    }

    fun disableAutoDownload(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .disableAutoDownload(podcast.origin)
        }
    }

    fun subscribe(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .subscribe(podcast.origin)
        }
    }

    fun unsubscribe(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcastSubscriptions()
                .unsubscribe(podcast.origin)
        }
    }

    fun deletePodcast(
        db: AppDatabase,
        podcast: PodcastModel
    ) {
        viewModelScope.launch {
            db.podcasts().delete(podcast)
        }
    }

}