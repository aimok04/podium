package app.podiumpodcasts.podium.ui.component.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorLayout(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.NewReleases,
    title: @Composable () -> String = { stringResource(R.string.layout_error_title) },
    content: @Composable () -> Unit
) {
    InfoLayout(
        modifier = modifier,
        icon = icon,
        title = title,
        content = content
    )
}