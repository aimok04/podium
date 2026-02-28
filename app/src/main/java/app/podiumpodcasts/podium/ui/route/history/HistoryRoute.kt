package app.podiumpodcasts.podium.ui.route.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastHistoryBundle
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItem
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItemActions
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.formatPubDate
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.PagerScaffold
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryRoute(
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = viewModel { HistoryViewModel(db) }

    val pager = remember { vm.historyElements }
        .collectAsLazyPagingItems()

    val todayPager = remember { vm.todayPager }
        .collectAsLazyPagingItems()

    val weekPager = remember { vm.weekPager }
        .collectAsLazyPagingItems()

    val monthPager = remember { vm.monthPager }
        .collectAsLazyPagingItems()

    val yearPager = remember { vm.yearPager }
        .collectAsLazyPagingItems()

    val olderPager = remember { vm.olderPager }
        .collectAsLazyPagingItems()

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
                    Text(stringResource(R.string.route_history))
                }
            )
        }
    ) { inset ->
        PagerScaffold(
            pager,
            isEmpty = {
                InfoLayout(
                    modifier = Modifier.padding(inset),
                    icon = Icons.Rounded.History,
                    title = { stringResource(R.string.route_history_empty_title) },
                ) {
                    Text(
                        text = stringResource(R.string.route_history_empty_text),
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            LazyColumn(
                Modifier
                    .padding(inset)
                    .fillMaxSize(),
                state = when(pager.itemCount) {
                    0 -> LazyListState()
                    else -> vm.lazyListState
                },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                pagerSection(
                    vm = vm,
                    label = R.string.route_history_today,
                    pager = todayPager,
                    onClickEpisode = onClickEpisode
                )

                pagerSection(
                    vm = vm,
                    label = R.string.route_history_this_week,
                    pager = weekPager,
                    onClickEpisode = onClickEpisode
                )

                pagerSection(
                    vm = vm,
                    label = R.string.route_history_this_month,
                    pager = monthPager,
                    onClickEpisode = onClickEpisode
                )

                pagerSection(
                    vm = vm,
                    label = R.string.route_history_this_year,
                    pager = yearPager,
                    onClickEpisode = onClickEpisode
                )

                pagerSection(
                    vm = vm,
                    label = R.string.route_history_older,
                    pager = olderPager,
                    onClickEpisode = onClickEpisode
                )

                item {
                    FloatingMediaPlayerSpacer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.pagerSection(
    vm: HistoryViewModel,
    label: Int,
    pager: LazyPagingItems<PodcastHistoryBundle>,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    if(pager.itemCount > 0) {
        item(
            key = "HEADER:$label"
        ) {
            Box(
                Modifier
                    .padding(12.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(label),
                    color = MaterialTheme.colorScheme.primary,
                    style = Typography.titleMediumEmphasized
                )
            }
        }

        items(
            count = pager.itemCount,
            key = { pager[it]?.history?.id ?: it }
        ) {
            val historyElement = pager[it] ?: return@items

            SwipeableItem(
                modifier = Modifier.animateItem(),
                endAction = SwipeableItemActions.DeleteAction {
                    vm.delete(historyElement)
                    false
                }
            ) {
                PodcastEpisodeListItem(
                    bundle = PodcastEpisodeBundle(
                        episode = historyElement.episode,
                        playState = historyElement.playState,
                        download = historyElement.download
                    ),

                    overlineText = formatPubDate(
                        LocalContext.current,
                        historyElement.history.timestamp / 1000L
                    ),
                    descriptionText = historyElement.episode.podcastTitle,

                    index = it,
                    count = pager.itemCount,

                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),

                    onClick = {
                        onClickEpisode(historyElement.episode)
                    }
                )
            }
        }
    }
}