package app.podiumpodcasts.podium.ui.vm.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.ListItemModelBundle
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.ListEditBottomSheetState
import kotlinx.coroutines.launch

class ListViewModel(
    val db: AppDatabase,
    val listId: Int
) : ViewModel() {

    val lazyListState = LazyListState()

    val listEditBottomSheetState = ListEditBottomSheetState()
    val showDeleteDialog = mutableStateOf(false)

    val items = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.listItems()
            .all(listId)
    }.flow

    fun deleteList() {
        viewModelScope.launch {
            db.lists().delete(listId)
        }
    }

    fun delete(item: ListItemModelBundle) {
        viewModelScope.launch {
            db.listItems().deleteAndReindex(
                listId = item.listItem.listId,
                itemId = item.listItem.id,
                deletedPosition = item.listItem.position
            )
        }
    }

    suspend fun move(from: Int, to: Int) {
        val id = db.listItems()._getItemIdAtPosition(listId, from)
        db.listItems().moveAndReindex(listId, id ?: 0, from, to)
    }

}