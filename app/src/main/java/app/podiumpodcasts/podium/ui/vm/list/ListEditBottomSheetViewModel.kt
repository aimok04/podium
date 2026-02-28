package app.podiumpodcasts.podium.ui.vm.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.db.AppDatabase
import kotlinx.coroutines.launch

interface ListEditBottomSheetUIState {
    object Idle : ListEditBottomSheetUIState
    data class Error(val reason: String) : ListEditBottomSheetUIState
}

class ListEditBottomSheetViewModel : ViewModel() {

    val state = mutableStateOf<ListEditBottomSheetUIState>(ListEditBottomSheetUIState.Idle)

    val done = mutableStateOf(false)

    val name = mutableStateOf("")
    val description = mutableStateOf("")

    fun edit(db: AppDatabase, listId: Int) {
        viewModelScope.launch {
            state.value = try {
                db.lists().edit(
                    id = listId,
                    name = name.value,
                    description = description.value
                )

                done.value = true
                ListEditBottomSheetUIState.Idle
            } catch(e: Exception) {
                e.printStackTrace()
                ListEditBottomSheetUIState.Error(e.toString())
            }
        }
    }

}