package app.podiumpodcasts.podium.ui.vm.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.ListModel
import kotlinx.coroutines.launch

interface ListCreateBottomSheetState {
    object Idle : ListCreateBottomSheetState
    data class Error(val reason: String) : ListCreateBottomSheetState
}

class ListCreateBottomSheetViewModel : ViewModel() {

    val state = mutableStateOf<ListCreateBottomSheetState>(ListCreateBottomSheetState.Idle)

    val done = mutableStateOf(false)

    val name = mutableStateOf("")
    val description = mutableStateOf("")

    fun create(db: AppDatabase) {
        viewModelScope.launch {
            state.value = try {
                db.lists().create(
                    ListModel(
                        name = name.value,
                        description = description.value
                    )
                )

                done.value = true
                ListCreateBottomSheetState.Idle
            } catch(e: Exception) {
                e.printStackTrace()
                ListCreateBottomSheetState.Error(e.toString())
            }
        }
    }

}