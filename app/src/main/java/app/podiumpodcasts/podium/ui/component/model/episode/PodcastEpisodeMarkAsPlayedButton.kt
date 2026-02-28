package app.podiumpodcasts.podium.ui.component.model.episode

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastEpisodeMarkAsPlayedButton(
    bundle: PodcastEpisodeBundle,
    colors: IconToggleButtonColors = IconButtonDefaults.filledIconToggleButtonColors()
) {
    val scope = rememberCoroutineScope()
    val db = LocalDatabase.current

    val playState = bundle.playState

    FilledIconToggleButton(
        checked = playState?.played == true,
        onCheckedChange = {
            if(playState == null) return@FilledIconToggleButton

            scope.launch {
                if(!playState.played) {
                    db.podcastEpisodes()
                        .unnewAndUpdateNewEpisodesCount(
                            origin = bundle.episode.origin,
                            episodeId = bundle.episode.id
                        )
                }

                if(playState.played) {
                    db.podcastEpisodePlayStates()
                        .saveState(bundle.episode.id, 0)

                    db.podcastEpisodePlayStates()
                        .savePlayed(bundle.episode.id, false)
                } else {
                    db.podcastEpisodePlayStates()
                        .savePlayed(bundle.episode.id, true)
                }
            }
        },

        colors = colors
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(R.string.common_action_mark_as_played)
        )
    }
}