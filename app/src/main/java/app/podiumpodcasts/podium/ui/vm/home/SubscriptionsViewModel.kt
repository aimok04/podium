package app.podiumpodcasts.podium.ui.vm.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase

class SubscriptionsViewModel(
    val db: AppDatabase
) : ViewModel() {

    val lazyGridState = LazyGridState()

    val subscriptions = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.podcastSubscriptions().allByNewEpisodes()
    }.flow

}