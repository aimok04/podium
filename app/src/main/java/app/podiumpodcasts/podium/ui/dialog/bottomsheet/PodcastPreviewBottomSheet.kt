package app.podiumpodcasts.podium.ui.dialog.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.ui.view.model.PodcastPreviewView
import kotlinx.coroutines.launch

class PodcastPreviewBottomSheetState {

    val shown = mutableStateOf(false)
    internal val model = mutableStateOf<PodcastPreviewModel?>(null)

    fun show(podcastPreviewModel: PodcastPreviewModel) {
        this.model.value = podcastPreviewModel
        this.shown.value = true
    }

    fun hide() {
        this.shown.value = false
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastPreviewBottomSheet(
    state: PodcastPreviewBottomSheetState,
    onOpenPodcast: (podcast: PodcastModel) -> Unit
) {
    val scope = rememberCoroutineScope()
    if(!state.shown.value) return

    state.model.value?.let { preview ->
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = { state.hide() },

            sheetState = sheetState,
            contentWindowInsets = { WindowInsets() },
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(0.8f)
            ) {
                PodcastPreviewView(
                    podcast = preview,

                    onOpenPodcast = onOpenPodcast,
                    onBack = {
                        scope.launch {
                            sheetState.hide()
                            state.hide()
                        }
                    }
                )
            }
        }
    }
}