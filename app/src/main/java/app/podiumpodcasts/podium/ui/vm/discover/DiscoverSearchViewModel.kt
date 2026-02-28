package app.podiumpodcasts.podium.ui.vm.discover

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.PodcastPreviewBottomSheetState
import kotlinx.coroutines.launch

interface DiscoverSearchState {
    class Idle() : DiscoverSearchState
    class Loading() : DiscoverSearchState
    class Empty() : DiscoverSearchState
    data class Done(
        val result: List<PodcastPreviewModel>
    ) : DiscoverSearchState

    data class Error(val error: String) : DiscoverSearchState
}

class DiscoverSearchViewModel(
    val countryCode: String,
    val db: AppDatabase
) : ViewModel() {

    private val applePodcastClient = ApplePodcastClient()

    val state = mutableStateOf<DiscoverSearchState>(DiscoverSearchState.Idle())
    val gridState = LazyGridState(0, 0)
    val podcastPreviewBottomSheetState = PodcastPreviewBottomSheetState()

    fun search(query: String) {
        if(query.isBlank()) {
            state.value = DiscoverSearchState.Idle()
            return
        }

        viewModelScope.launch {
            state.value = DiscoverSearchState.Loading()
            state.value = try {
                val result = applePodcastClient.search.search(
                    query = query,
                    countryCode = countryCode
                )

                if(result.isEmpty())
                    DiscoverSearchState.Empty()
                else
                    DiscoverSearchState.Done(result)
            } catch(e: Exception) {
                e.printStackTrace()
                DiscoverSearchState.Error(e.toString())
            }
        }
    }

}