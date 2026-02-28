package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackButton(
    icon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    onClick: () -> Unit
) {
    BubbleButton(
        icon = icon,
        contentDescription = stringResource(R.string.common_action_back),
        onClick = onClick
    )
}