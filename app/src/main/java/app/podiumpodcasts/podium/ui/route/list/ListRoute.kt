package app.podiumpodcasts.podium.ui.route.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.common.BubbleButton
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItem
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItemActions
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.list.ListItemListItem
import app.podiumpodcasts.podium.ui.dialog.DeleteConfirmationDialog
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.ListEditBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.PagerScaffold
import app.podiumpodcasts.podium.ui.vm.list.ListViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListRoute(
    listId: Int,

    onClickPodcast: (origin: String) -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit,

    onBack: () -> Unit
) {
    val db = LocalDatabase.current

    val vm = viewModel(key = listId.toString()) { ListViewModel(db, listId) }

    val list = db.lists().get(listId).collectAsState(null)
    val items = vm.items.collectAsLazyPagingItems()

    val systemList = list.value?.systemList()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val reorderableLazyGridState = rememberReorderableLazyListState(
        vm.lazyListState
    ) { from, to ->
        vm.move(from.index - 1, to.index - 1)
    }

    Scaffold(
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton { onBack() }
                },
                title = {
                    Text(
                        text = systemList?.label?.let { stringResource(it) }
                            ?: list.value?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if(list.value?.isSystemList == false) {
                        BubbleButton(
                            icon = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.common_action_edit)
                        ) {
                            vm.listEditBottomSheetState.show(listId)
                        }

                        BubbleButton(
                            icon = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.common_action_delete)
                        ) {
                            vm.showDeleteDialog.value = true
                        }
                    }
                }
            )
        }
    ) { inset ->
        PagerScaffold(
            items,
            isEmpty = {
                InfoLayout(
                    modifier = Modifier.padding(inset),
                    icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    title = { "Empty list" },
                ) {
                    Text(
                        text = "No episodes or podcasts have been added to this collection yet.",
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            LazyColumn(
                state = when(items.itemCount) {
                    0 -> LazyListState()
                    else -> vm.lazyListState
                },
                modifier = Modifier
                    .padding(inset)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                item {
                    if(systemList == null) {
                        if((list.value?.description ?: "").isNotBlank()) Column {
                            Text(
                                text = list.value?.description ?: ""
                            )

                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                items(
                    count = items.itemCount,
                    key = { items[it]!!.listItem.id }
                ) { index ->
                    val item = items[index] ?: return@items

                    ReorderableItem(
                        state = reorderableLazyGridState,
                        key = item.listItem.id
                    ) {
                        SwipeableItem(
                            modifier = Modifier
                                .animateItem()
                                .longPressDraggableHandle(),
                            endAction = SwipeableItemActions.DeleteAction {
                                vm.delete(item)
                                false
                            }
                        ) {
                            ListItemListItem(
                                listItem = item,

                                index = index,
                                count = items.itemCount,

                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),

                                onClickPodcast = onClickPodcast,
                                onClickEpisode = onClickEpisode
                            )
                        }
                    }
                }

                item {
                    FloatingMediaPlayerSpacer()
                }
            }
        }
    }

    ListEditBottomSheet(
        state = vm.listEditBottomSheetState
    )

    if(vm.showDeleteDialog.value) DeleteConfirmationDialog(
        onDismiss = {
            vm.showDeleteDialog.value = false
        },
        itemName = list.value?.name ?: "",
        additionalText = stringResource(R.string.dialog_delete_confirmation_all_data_will_be_lost),
        onConfirm = {
            vm.deleteList()
            onBack()
        }
    )
}