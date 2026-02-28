package app.podiumpodcasts.podium.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

data class DetailsListItemModel(
    val icon: ImageVector,
    @StringRes val label: Int,
    val value: String,
    val content: (@Composable () -> Unit) = { },
    val onClick: () -> Unit = { }
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailsList(
    items: List<DetailsListItemModel>
) {
    val displayedItems = remember { mutableStateListOf<DetailsListItemModel>() }
    LaunchedEffect(items) {
        displayedItems.clear()
        displayedItems.addAll(items.filter { it.value.isNotBlank() })
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        displayedItems.forEachIndexed { index, item ->
            SegmentedListItem(
                onClick = item.onClick,
                shapes = ListItemDefaults.segmentedShapes(index, displayedItems.size),
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.label),
                    )
                },
                overlineContent = {
                    Text(
                        text = stringResource(item.label)
                    )
                }
            ) {
                SelectionContainer {
                    Text(
                        text = item.value
                    )
                }

                item.content()
            }
        }
    }
}