package app.podiumpodcasts.podium.ui.dialog.bottomsheet.podcast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesFilter
import app.podiumpodcasts.podium.ui.component.model.podcast.PodcastSearchFilterOrderBarState
import kotlinx.coroutines.launch

private enum class FilterItems(
    val icon: ImageVector,
    val label: Int,
    val labelInverted: Int,
    val filter: PodcastEpisodesFilter
) {
    PLAYED(
        icon = Icons.Rounded.PlayCircleOutline,
        label = R.string.filter_played,
        labelInverted = R.string.filter_played_inverted,
        filter = PodcastEpisodesFilter.PLAYED
    ),
    PAUSED(
        icon = Icons.Rounded.Pause,
        label = R.string.filter_paused,
        labelInverted = R.string.filter_paused_inverted,
        filter = PodcastEpisodesFilter.PAUSED
    ),
    NEW(
        icon = Icons.Rounded.NewReleases,
        label = R.string.filter_new,
        labelInverted = R.string.filter_new_inverted,
        filter = PodcastEpisodesFilter.NEW
    ),
    FAVORITE(
        icon = Icons.Rounded.Star,
        label = R.string.filter_favorite,
        labelInverted = R.string.filter_favorite_inverted,
        filter = PodcastEpisodesFilter.FAVORITE
    ),
    DOWNLOADED(
        icon = Icons.Rounded.Download,
        label = R.string.filter_downloaded,
        labelInverted = R.string.filter_downloaded_inverted,
        filter = PodcastEpisodesFilter.DOWNLOADED
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastFilterBottomSheet(
    state: PodcastSearchFilterOrderBarState,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            items(FilterItems.entries.size) {
                val item = FilterItems.entries[it]

                val isNegative = state.negativeFilter.contains(item.filter)
                val isChecked = isNegative || state.filter.contains(item.filter)

                SegmentedListItem(
                    checked = isChecked,
                    onCheckedChange = {
                        if(state.filter.contains(item.filter)) {
                            state.filter.remove(item.filter)
                            state.negativeFilter.add(item.filter)
                        } else if(state.negativeFilter.contains(item.filter)) {
                            state.negativeFilter.remove(item.filter)
                        } else {
                            state.filter.add(item.filter)
                        }
                    },

                    shapes = ListItemDefaults.segmentedShapes(
                        index = it,
                        count = FilterItems.entries.size
                    ),

                    colors = when(isNegative) {
                        true -> ListItemDefaults.segmentedColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedLeadingContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedTrailingContentColor = MaterialTheme.colorScheme.onErrorContainer
                        )

                        else -> ListItemDefaults.segmentedColors()
                    },

                    leadingContent = {
                        Icon(item.icon, stringResource(item.label))
                    },
                    trailingContent = {
                        AnimatedVisibility(
                            visible = isNegative,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Icon(Icons.Rounded.Remove, stringResource(R.string.common_negative))
                        }
                    }
                ) {
                    AnimatedContent(
                        targetState = isNegative
                    ) { isNegative ->
                        Text(
                            stringResource(
                                when(isNegative) {
                                    false -> item.label
                                    true -> item.labelInverted
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}