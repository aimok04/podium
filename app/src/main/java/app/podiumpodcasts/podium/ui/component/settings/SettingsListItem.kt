package app.podiumpodcasts.podium.ui.component.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsListItem(
    icon: @Composable () -> Unit,
    label: String = "",
    description: String = "",

    selected: Boolean = false,
    enabled: Boolean = true,

    index: Int = 0,
    count: Int = 1,

    leadingContent: @Composable () -> Unit = {
        Box(
            Modifier.padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    },
    content: @Composable () -> Unit = {
        Text(
            text = label
        )
    },
    supportingContent: @Composable (() -> Unit)? = {
        Text(
            text = description
        )
    },
    trailingContent: @Composable (() -> Unit)? = null,

    onClick: () -> Unit
) {
    SegmentedListItem(
        modifier = if(count == 1)
            Modifier.clip(RoundedCornerShape(16.dp))
        else
            Modifier,

        selected = selected,
        enabled = enabled,

        onClick = onClick,

        leadingContent = leadingContent,
        content = content,
        supportingContent = supportingContent,
        trailingContent = trailingContent,

        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count
        ),

        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}