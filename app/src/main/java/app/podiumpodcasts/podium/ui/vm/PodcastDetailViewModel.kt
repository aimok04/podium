package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesFilter
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrder
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrderBy
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.background.work.SingularPodcastUpdateWork
import app.podiumpodcasts.podium.manager.SubscriptionManager
import app.podiumpodcasts.podium.ui.component.model.podcast.PodcastSearchFilterOrderBarState
import app.podiumpodcasts.podium.ui.view.model.Destinations
import coil3.Image
import coil3.asDrawable
import com.materialkolor.ktx.themeColorOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

private data class QueryBundle(
    val search: String,
    val orderBy: PodcastEpisodesOrderBy,
    val order: PodcastEpisodesOrder,
    val filter: Set<PodcastEpisodesFilter>,
    val filterNot: Set<PodcastEpisodesFilter>
)

class PodcastDetailViewModel(
    val db: AppDatabase,
    val podcast: PodcastModel
) : ViewModel() {

    val subscriptionManager = SubscriptionManager(
        db = db
    )

    val searchFilterOrderBarState = PodcastSearchFilterOrderBarState()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val episodePager =
        snapshotFlow {
            QueryBundle(
                search = searchFilterOrderBarState.searchQuery.value,
                orderBy = searchFilterOrderBarState.orderBy.value,
                order = searchFilterOrderBarState.order.value,
                filter = searchFilterOrderBarState.filter.toSet(),
                filterNot = searchFilterOrderBarState.negativeFilter.toSet()
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { q ->
                Pager(PagingConfig(pageSize = 15)) {
                    val query = db.podcastEpisodes().buildQuery(
                        origin = podcast.origin,
                        searchQuery = q.search,
                        orderBy = q.orderBy,
                        order = q.order,
                        filter = q.filter,
                        filterNot = q.filterNot
                    )

                    db.podcastEpisodes().queryPaged(query)
                }.flow
            }
            .cachedIn(viewModelScope)

    val subscription =
        db.podcastSubscriptions().get(podcast.origin)

    var selectedDestination by mutableStateOf(Destinations.EPISODES)

    var isRefreshing by mutableStateOf(false)

    val showSettingsBottomSheet = mutableStateOf(false)
    val showDeleteDialog = mutableStateOf(false)

    val lazyListState = LazyListState()
    val snackbarHostState = SnackbarHostState()

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
            subscriptionManager.subscribe(podcast.origin)
        }
    }

    fun unsubscribe() {
        viewModelScope.launch {
            subscriptionManager.unsubscribe(podcast.origin)
        }
    }

    fun deletePodcast() {
        viewModelScope.launch {
            if(db.podcastSubscriptions().getSync(podcast.origin) != null) {
                subscriptionManager.unsubscribe(podcast.origin)
            }

            db.podcasts().delete(podcast)
        }
    }

    fun markAsPlayed(bundle: PodcastEpisodeBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .savePlayed(bundle.episode.id, true)

            db.syncActions()
                .addPlayState(
                    origin = bundle.episode.origin,
                    episodeId = bundle.episode.id,
                    audioUrl = bundle.episode.audioUrl,
                    duration = bundle.episode.duration,
                    state = bundle.playState?.state ?: 0,
                    played = true
                )
        }
    }

    fun markAsUnplayed(bundle: PodcastEpisodeBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .savePlayed(bundle.episode.id, false)

            db.syncActions()
                .addPlayState(
                    origin = bundle.episode.origin,
                    episodeId = bundle.episode.id,
                    audioUrl = bundle.episode.audioUrl,
                    duration = bundle.episode.duration,
                    state = bundle.playState?.state ?: 0,
                    played = false
                )
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