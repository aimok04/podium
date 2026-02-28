package app.podiumpodcasts.podium.ui.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StateIconLayout(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Check,
    contentDescription: String = stringResource(R.string.common_done),
    timeout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(Unit) {
            scope.launch {
                delay(500)
                timeout()
            }
        }

        Box(
            Modifier
                .size(96.dp)
                .clip(MaterialShapes.Cookie12Sided.toShape())
                .background(LoadingIndicatorDefaults.containedContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = LoadingIndicatorDefaults.containedIndicatorColor
            )
        }
    }
}