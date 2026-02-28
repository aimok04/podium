package app.podiumpodcasts.podium.ui.route.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItem
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItemActions
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.PagerScaffold
import app.podiumpodcasts.podium.ui.vm.home.ContinuePlayingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContinuePlayingRoute(
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = viewModel { ContinuePlayingViewModel(db) }

    val continuePlaying = vm.continuePlaying.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton { onBack() }
                },
                title = {
                    Text(stringResource(R.string.route_continue_playing))
                }
            )
        }
    ) { inset ->
        PagerScaffold(
            continuePlaying,
            isEmpty = {
                InfoLayout(
                    modifier = Modifier.padding(inset),
                    icon = Icons.Rounded.DoneAll,
                    title = { stringResource(R.string.route_continue_playing_empty_title) },
                ) {
                    Text(
                        text = stringResource(R.string.route_continue_playing_empty_text),
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            LazyColumn(
                Modifier
                    .padding(inset)
                    .fillMaxSize(),
                state = when(continuePlaying.itemCount) {
                    0 -> LazyListState()
                    else -> vm.lazyListState
                },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                items(
                    continuePlaying.itemCount,
                    key = { continuePlaying[it]!!.episode.id }
                ) {
                    val item = continuePlaying[it] ?: return@items

                    SwipeableItem(
                        modifier = Modifier.animateItem(),
                        startAction = SwipeableItemActions.CheckAction(
                            onAction = {
                                vm.markAsPlayed(item)
                                false
                            }
                        ),
                        endAction = SwipeableItemActions.ResetAction(
                            onAction = {
                                vm.resetPlayState(item)
                                false
                            }
                        )
                    ) {
                        PodcastEpisodeListItem(
                            bundle = item.toPodcastEpisodeBundle(),

                            descriptionText = item.episode.podcastTitle,

                            index = it,
                            count = continuePlaying.itemCount,

                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),

                            onClick = {
                                onClickEpisode(item.episode)
                            }
                        )
                    }
                }

                item {
                    FloatingMediaPlayerSpacer()
                }
            }
        }
    }
}