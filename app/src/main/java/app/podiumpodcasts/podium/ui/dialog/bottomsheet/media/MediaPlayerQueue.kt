package app.podiumpodcasts.podium.ui.dialog.bottomsheet.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.ui.component.common.SwitchableDynamicMaterialExpressiveTheme
import app.podiumpodcasts.podium.ui.component.media.MediaItemListItem
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.MediaPlayerViewModel
import app.podiumpodcasts.podium.utils.getEpisodeId
import app.podiumpodcasts.podium.utils.getOrigin
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPlayerQueueBottomSheet(
    onOpenEpisode: (
        origin: String,
        id: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val settingsRepository = LocalSettingsRepository.current
    val vm = viewModel<MediaPlayerViewModel>()

    val enableArtworkColors = settingsRepository.appearance.enableArtworkColors.collectAsState(true)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(vm.queueIndex.toList()) {
        if(vm.queueIndex.isNotEmpty()) return@LaunchedEffect

        sheetState.hide()
        onDismiss()
    }

    SwitchableDynamicMaterialExpressiveTheme(
        enable = enableArtworkColors.value,
        seedColor = Color(vm.metadataImageSeedColor ?: 1)
    ) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Next up",
                        style = Typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }

                items(
                    count = vm.queueIndex.size,
                    key = { index ->
                        vm.queueIndex[index]
                    }
                ) { queueIndexIndex ->
                    val index = vm.queueIndex[queueIndexIndex]
                    val mediaItem = vm.rememberMediaItemAt(index)

                    mediaItem?.let {
                        MediaItemListItem(
                            mediaItem = mediaItem,
                            index = queueIndexIndex,
                            count = vm.queueIndex.size,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                    onOpenEpisode(
                                        mediaItem.getOrigin(),
                                        mediaItem.getEpisodeId()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}