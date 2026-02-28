package app.podiumpodcasts.podium.ui.dialog.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.list.SaveToListBottomSheetViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SaveToListBottomSheet(
    contentId: String,
    isPodcast: Boolean,
    onDismiss: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = viewModel(key = contentId) {
        SaveToListBottomSheetViewModel(
            db = db,
            contentId = contentId,
            isPodcast = isPodcast
        )
    }

    val lists = vm.lists.collectAsLazyPagingItems()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            items(lists.itemCount) { index ->
                lists[index]?.let { item ->
                    val systemList = item.list.systemList()
                    if((systemList?.onlyEpisodes == true) && isPodcast) return@items

                    SegmentedListItem(
                        checked = item.contains,
                        onCheckedChange = {
                            vm.toggle(item)
                        },

                        leadingContent = {
                            Checkbox(
                                checked = item.contains,
                                onCheckedChange = {
                                    vm.toggle(item)
                                }
                            )
                        },

                        content = {
                            Text(
                                text = systemList?.label?.let { stringResource(it) }
                                    ?: item.list.name
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    R.string.template_elements,
                                    item.list.itemCount
                                )
                            )
                        },

                        trailingContent = systemList?.icon?.let {
                            {
                                Icon(it, "")
                            }
                        },

                        shapes = ListItemDefaults.segmentedShapes(
                            count = lists.itemCount,
                            index = index,
                        )
                    )
                }
            }
        }
    }
}