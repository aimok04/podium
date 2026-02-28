package app.podiumpodcasts.podium.ui.vm.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import kotlinx.coroutines.launch

class NewEpisodesViewModel(
    val db: AppDatabase
) : ViewModel() {

    val lazyListState = LazyListState()

    val newEpisodes = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.podcastEpisodes().allNew()
    }.flow

    fun unnew(item: PodcastEpisodeBundle) {
        viewModelScope.launch {
            db.podcastEpisodes()
                .unnewAndUpdateNewEpisodesCount(item.episode.origin, item.episode.id)
        }
    }

}