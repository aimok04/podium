package app.podiumpodcasts.podium.ui.component.model.episode

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadState
import app.podiumpodcasts.podium.manager.DownloadManager
import app.podiumpodcasts.podium.ui.component.button.StateDisplayingIconToggleButton
import app.podiumpodcasts.podium.ui.component.button.StateDisplayingToggleButton
import app.podiumpodcasts.podium.ui.component.common.ButtonLabelWithIconInset
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.DownloadManagementBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import kotlinx.coroutines.launch

private val stateMap = mapOf(
    (-1)
            to (Pair(Icons.Rounded.Download, R.string.common_action_download)),

    PodcastEpisodeDownloadState.NOT_DOWNLOADED.value
            to (Pair(Icons.Rounded.DownloadForOffline, R.string.common_waiting)),

    PodcastEpisodeDownloadState.DOWNLOADING.value
            to (Pair(Icons.Rounded.Downloading, R.string.common_downloading)),

    PodcastEpisodeDownloadState.DOWNLOADED.value
            to (Pair(Icons.Rounded.DownloadDone, R.string.common_downloaded))
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastEpisodeDownloadButton(
    icon: Boolean,
    bundle: PodcastEpisodeBundle
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = LocalDatabase.current

    val showDownloadManagementBottomSheet = remember { mutableStateOf(false) }

    val animateState = remember { Animatable(0f) }
    val firstAnim = remember { mutableStateOf(true) }

    LaunchedEffect(bundle) {
        val targetValue = when(bundle.download?.state) {
            null -> 0f
            PodcastEpisodeDownloadState.DOWNLOADING.value -> (
                    bundle.download.progress.toFloat() /
                            bundle.download.size.coerceAtLeast(1)
                    )

            else -> 1f
        }

        if(firstAnim.value) {
            animateState.snapTo(targetValue)
            firstAnim.value = false
        } else {
            animateState.animateTo(
                targetValue = targetValue,
                animationSpec = tween(250)
            )
        }
    }

    fun onCheckedChange() {
        scope.launch {
            if(bundle.download == null) {
                DownloadManager.downloadEpisode(context, db, bundle.episode.id)
            } else {
                showDownloadManagementBottomSheet.value = true
            }
        }
    }

    @Composable
    fun inner() {
        AnimatedContent(bundle.download?.state) { state ->
            val values = stateMap[state ?: -1]

            if(icon) {
                Icon(
                    imageVector = values!!.first,
                    contentDescription = stringResource(values.second)
                )
            } else {
                ButtonLabelWithIconInset(
                    icon = values!!.first,
                    label = stringResource(values.second)
                )
            }
        }
    }

    if(icon) {
        StateDisplayingIconToggleButton(
            state = animateState.value,
            minimumState = 0f,
            checked = bundle.download != null,
            colors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
            onCheckedChange = { onCheckedChange() },
            content = { inner() }
        )
    } else {
        StateDisplayingToggleButton(
            state = animateState.value,
            minimumState = 0f,
            checked = bundle.download != null,
            colors = ToggleButtonDefaults.tonalToggleButtonColors(),
            onCheckedChange = { onCheckedChange() },
            content = { inner() }
        )
    }

    if(showDownloadManagementBottomSheet.value) DownloadManagementBottomSheet(
        bundle = bundle,
        onDismiss = {
            showDownloadManagementBottomSheet.value = false
        }
    )
}