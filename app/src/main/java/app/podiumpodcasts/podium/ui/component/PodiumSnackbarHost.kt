package app.podiumpodcasts.podium.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.podiumpodcasts.podium.ui.component.media.LocalFloatingMediaPlayerHeight

/**
 * A snackbar host but respecting the floating media player height and adjusted design
 */
@Composable
fun PodiumSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = {
        Snackbar(
            snackbarData = it,
            shape = CircleShape
        )
    },
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(
            bottom = LocalFloatingMediaPlayerHeight.current
        ),
        snackbar = snackbar
    )
}