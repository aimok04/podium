package app.podiumpodcasts.podium.ui.vm.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastPlayStateBundle
import kotlinx.coroutines.launch

class ContinuePlayingViewModel(
    val db: AppDatabase
) : ViewModel() {

    val lazyListState = LazyListState()

    val continuePlaying = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.podcastEpisodePlayStates().allContinuePlaying()
    }.flow

    fun markAsPlayed(item: PodcastPlayStateBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .savePlayed(item.episode.id, true)
        }
    }

    fun resetPlayState(item: PodcastPlayStateBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .savePlayed(item.episode.id, false)
            db.podcastEpisodePlayStates()
                .saveState(item.episode.id, 0)
        }
    }

}