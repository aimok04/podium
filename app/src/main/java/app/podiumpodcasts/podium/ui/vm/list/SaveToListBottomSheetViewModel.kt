package app.podiumpodcasts.podium.ui.vm.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.ListWithContains
import kotlinx.coroutines.launch

class SaveToListBottomSheetViewModel(
    val db: AppDatabase,
    val contentId: String,
    val isPodcast: Boolean
) : ViewModel() {

    val lists = Pager(
        PagingConfig(
            pageSize = 15
        )
    ) {
        db.lists().allWithContains(contentId)
    }.flow

    fun toggle(item: ListWithContains) {
        viewModelScope.launch {
            if(item.contains) {
                val item = db.listItems().get(
                    listId = item.list.id,
                    contentId = contentId
                )

                db.listItems().deleteAndReindex(
                    listId = item.listId,
                    itemId = item.id,
                    deletedPosition = item.position
                )
            } else {
                val position = db.listItems().getNextPosition(item.list.id)
                    ?: 0

                db.listItems().addListItemAndRefreshItemCount(
                    listId = item.list.id,
                    contentId = contentId,
                    isPodcast = isPodcast,
                    position = position
                )
            }
        }
    }

}