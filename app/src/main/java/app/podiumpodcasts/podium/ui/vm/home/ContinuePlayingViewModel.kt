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

            db.syncActions()
                .addPlayState(
                    origin = item.episode.origin,
                    episodeId = item.episode.id,
                    audioUrl = item.episode.audioUrl,
                    duration = item.episode.duration,
                    state = item.playState.state,
                    played = true
                )
        }
    }

    fun resetPlayState(item: PodcastPlayStateBundle) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates()
                .set(item.episode.id, 0, false)

            db.syncActions()
                .addPlayState(
                    origin = item.episode.origin,
                    episodeId = item.episode.id,
                    audioUrl = item.episode.audioUrl,
                    duration = item.episode.duration,
                    state = 0,
                    played = false
                )
        }
    }

    fun setPlayState(item: PodcastPlayStateBundle, played: Boolean, state: Int) {
        viewModelScope.launch {
            db.podcastEpisodePlayStates().set(
                item.episode.id, state, played
            )

            db.syncActions()
                .addPlayState(
                    origin = item.episode.origin,
                    episodeId = item.episode.id,
                    audioUrl = item.episode.audioUrl,
                    duration = item.episode.duration,
                    state = state,
                    played = played
                )

        }
    }

}