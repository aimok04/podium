package app.podiumpodcasts.podium.ui.component.model.episode

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.ui.component.button.StateDisplayingToggleButton
import app.podiumpodcasts.podium.ui.component.common.ButtonLabelWithIconInset
import app.podiumpodcasts.podium.ui.formatEpisodePlayTime
import app.podiumpodcasts.podium.ui.vm.MediaPlayerViewModel

private enum class ButtonState {
    PLAYING,
    PLAYED,
    NOT_PLAYING
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastEpisodePlayButton(
    bundle: PodcastEpisodeBundle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight),
    interactionSource: MutableInteractionSource? = null
) {
    val context = LocalContext.current
    val vm = viewModel<MediaPlayerViewModel>()

    val episode = bundle.episode
    val playState = bundle.playState

    val isCurrentlyPlaying = vm.metadataEpisodeId == episode.id

    val buttonState = when(isCurrentlyPlaying && vm.isPlaying) {
        true -> ButtonState.PLAYING
        false -> when(playState!!.played) {
            true -> ButtonState.PLAYED
            false -> ButtonState.NOT_PLAYING
        }
    }

    StateDisplayingToggleButton(
        state = when(buttonState) {
            ButtonState.PLAYED -> 1f
            else -> playState!!.state / episode.duration.toFloat()
        },
        minimumState = 0.03f,
        checked = isCurrentlyPlaying,
        onCheckedChange = {
            if(!isCurrentlyPlaying) {
                vm.play(context, bundle)
            } else if(vm.isPlaying) {
                vm.pause()
            } else {
                vm.play()
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        AnimatedContent(buttonState) { state ->
            ButtonLabelWithIconInset(
                icon = when(state) {
                    ButtonState.PLAYING -> Icons.Rounded.Pause
                    ButtonState.PLAYED -> Icons.Rounded.PlayCircleOutline
                    ButtonState.NOT_PLAYING -> Icons.Rounded.PlayArrow
                },
                label = when(state) {
                    ButtonState.PLAYED -> stringResource(R.string.common_state_played)
                    else -> formatEpisodePlayTime(
                        context = context,
                        duration = episode.duration,
                        state = playState!!.state
                    )
                }
            )
        }
    }
}