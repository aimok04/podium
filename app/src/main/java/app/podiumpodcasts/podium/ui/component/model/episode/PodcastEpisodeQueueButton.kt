package app.podiumpodcasts.podium.ui.component.model.episode

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.ui.vm.MediaPlayerViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastEpisodeQueueButton(
    bundle: PodcastEpisodeBundle
) {
    val context = LocalContext.current
    val vm = viewModel<MediaPlayerViewModel>()

    val inQueue = vm.queue.contains(bundle.episode.id)

    AnimatedVisibility(
        visible = vm.isPlayerVisible,
        enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        FilledTonalIconToggleButton(
            checked = inQueue,
            onCheckedChange = {
                if(inQueue) {
                    vm.dequeue(bundle.episode.id)
                } else {
                    vm.enqueue(context, bundle)
                }
            }
        ) {
            AnimatedContent(
                targetState = inQueue
            ) { inQueue ->
                when(inQueue) {
                    true -> Icon(
                        imageVector = Icons.Rounded.PlaylistRemove,
                        contentDescription = stringResource(R.string.common_action_remove_from_queue)
                    )

                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = stringResource(R.string.common_action_add_to_queue)
                    )
                }
            }
        }
    }
}