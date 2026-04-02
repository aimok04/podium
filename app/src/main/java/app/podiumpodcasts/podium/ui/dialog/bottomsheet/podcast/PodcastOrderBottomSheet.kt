package app.podiumpodcasts.podium.ui.dialog.bottomsheet.podcast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrder
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrderBy
import app.podiumpodcasts.podium.ui.component.model.podcast.PodcastSearchFilterOrderBarState
import kotlinx.coroutines.launch

private enum class OrderByItems(
    val icon: ImageVector,
    val label: Int,
    val orderBy: PodcastEpisodesOrderBy
) {
    DATE(
        icon = Icons.Rounded.AccessTime,
        label = R.string.orderby_date,
        orderBy = PodcastEpisodesOrderBy.DATE
    ),
    TITLE(
        icon = Icons.Rounded.AccessTime,
        label = R.string.orderby_title,
        orderBy = PodcastEpisodesOrderBy.TITLE
    ),
    DURATION(
        icon = Icons.Rounded.HourglassBottom,
        label = R.string.orderby_duration,
        orderBy = PodcastEpisodesOrderBy.DURATION
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastOrderBottomSheet(
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
            items(OrderByItems.entries.size) {
                val item = OrderByItems.entries[it]

                val isChecked = state.orderBy.value == item.orderBy

                SegmentedListItem(
                    checked = isChecked,
                    onCheckedChange = {
                        state.orderBy.value = when(isChecked) {
                            true -> PodcastEpisodesOrderBy.DATE
                            false -> item.orderBy
                        }
                    },

                    shapes = ListItemDefaults.segmentedShapes(
                        index = it,
                        count = OrderByItems.entries.size
                    ),

                    leadingContent = {
                        Icon(item.icon, stringResource(item.label))
                    },
                    trailingContent = {
                        Box(
                            Modifier.height(40.dp)
                        )

                        AnimatedVisibility(
                            visible = isChecked,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(
                                onClick = {
                                    state.order.value = when(state.order.value) {
                                        PodcastEpisodesOrder.ASCENDING -> PodcastEpisodesOrder.DESCENDING
                                        else -> PodcastEpisodesOrder.ASCENDING
                                    }
                                }
                            ) {
                                AnimatedContent(
                                    targetState = state.order.value
                                ) { order ->
                                    when(order) {
                                        PodcastEpisodesOrder.ASCENDING -> {
                                            Icon(
                                                Icons.Rounded.ArrowUpward,
                                                stringResource(R.string.common_ascending)
                                            )
                                        }

                                        PodcastEpisodesOrder.DESCENDING -> {
                                            Icon(
                                                Icons.Rounded.ArrowDownward,
                                                stringResource(R.string.common_descending)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(stringResource(item.label))
                }
            }
        }
    }
}