package app.podiumpodcasts.podium.ui.route.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadState
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.formatFileSize
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.PagerScaffold
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.DownloadsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadsRoute(
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = viewModel { DownloadsViewModel(db) }

    val totalSize = vm.totalSize.collectAsState(0L)
    val pager = remember { vm.downloads }.collectAsLazyPagingItems()

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
                    Text(stringResource(R.string.route_downloads))
                },
                actions = {
                    if(totalSize.value > 0L) Surface(
                        Modifier.padding(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = formatFileSize(totalSize.value),
                            style = Typography.bodyMedium
                        )
                    }
                }
            )
        }
    ) { inset ->
        PagerScaffold(
            pager,
            isEmpty = {
                InfoLayout(
                    modifier = Modifier.padding(inset),
                    icon = Icons.Rounded.Cloud,
                    title = { stringResource(R.string.route_downloads_empty_title) },
                ) {
                    Text(
                        text = stringResource(R.string.route_downloads_empty_text),
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
                items(
                    count = pager.itemCount,
                    key = { index ->
                        pager[index]!!.download.episodeId
                    }
                ) {
                    val bundle = pager[it] ?: return@items

                    PodcastEpisodeListItem(
                        modifier = Modifier.animateItem(),

                        bundle = PodcastEpisodeBundle(
                            episode = bundle.episode,
                            playState = bundle.playState,
                            download = bundle.download
                        ),

                        descriptionText = bundle.episode.podcastTitle,

                        trailingContent = {
                            if(bundle.download.state != PodcastEpisodeDownloadState.NOT_DOWNLOADED.value) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = when(bundle.download.state) {
                                            PodcastEpisodeDownloadState.DOWNLOADING.value ->
                                                formatFileSize(bundle.download.progress)

                                            else ->
                                                formatFileSize(bundle.download.size)
                                        },
                                        style = Typography.bodyMedium
                                    )
                                }
                            }
                        },

                        index = it,
                        count = pager.itemCount,

                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),

                        onClick = {
                            onClickEpisode(bundle.episode)
                        }
                    )
                }

                item {
                    FloatingMediaPlayerSpacer()
                }
            }
        }
    }
}