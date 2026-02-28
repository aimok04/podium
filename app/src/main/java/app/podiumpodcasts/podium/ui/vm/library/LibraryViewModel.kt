package app.podiumpodcasts.podium.ui.vm.library

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase

class LibraryViewModel(
    val db: AppDatabase
) : ViewModel() {

    val showListCreateBottomSheet = mutableStateOf(false)

    val lazyGridState = LazyGridState()

    val systemLists = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.lists().allSystem()
    }.flow

    val nonSystemLists = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.lists().allNonSystem()
    }.flow

}