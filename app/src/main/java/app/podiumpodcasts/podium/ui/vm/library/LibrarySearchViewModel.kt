package app.podiumpodcasts.podium.ui.vm.library

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import kotlinx.coroutines.flow.Flow

interface LibrarySearchState {
    class Idle() : LibrarySearchState
    data class Done(
        val podcastsPager: Flow<PagingData<PodcastModel>>,
        val episodesPager: Flow<PagingData<PodcastEpisodeBundle>>
    ) : LibrarySearchState
}

class LibrarySearchViewModel(
    val db: AppDatabase
) : ViewModel() {

    val state = mutableStateOf<LibrarySearchState>(LibrarySearchState.Idle())

    fun search(query: String) {
        state.value = LibrarySearchState.Done(
            podcastsPager = Pager(
                PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false
                )
            ) {
                db.podcasts().search(query)
            }.flow,
            episodesPager = Pager(
                PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false
                )
            ) {
                db.podcastEpisodes().search(query)
            }.flow
        )
    }

}