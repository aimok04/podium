package app.podiumpodcasts.podium.ui.route.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.layout.Section
import app.podiumpodcasts.podium.ui.component.layout.SectionCarousel
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerBreakpoint
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.media.LocalFloatingMediaPlayerHeight
import app.podiumpodcasts.podium.ui.component.model.PodcastCard
import app.podiumpodcasts.podium.ui.component.model.SubscriptionCard
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.helper.PagerScaffold
import app.podiumpodcasts.podium.ui.vm.home.ContinuePlayingViewModel
import app.podiumpodcasts.podium.ui.vm.home.LocallyAvailableViewModel
import app.podiumpodcasts.podium.ui.vm.home.NewEpisodesViewModel
import app.podiumpodcasts.podium.ui.vm.home.SubscriptionsViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeRoute(
    onSettings: () -> Unit,

    onClickSubscriptions: () -> Unit,
    onClickContinuePlaying: () -> Unit,
    onClickNewEpisodes: () -> Unit,
    onClickLocallyAvailable: () -> Unit,

    onClickAddPodcast: () -> Unit,

    onClickDiscover: () -> Unit,

    onClickPodcast: (origin: String) -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    val db = LocalDatabase.current

    val settingsRepository = LocalSettingsRepository.current

    val useAlternativeBranding = settingsRepository.appearance.useAlternativeBranding
        .collectAsState(false)

    val subscriptionsVm = viewModel { SubscriptionsViewModel(db) }
    val continuePlayingVm = viewModel { ContinuePlayingViewModel(db) }
    val newEpisodesVm = viewModel { NewEpisodesViewModel(db) }
    val locallyAvailableVm = viewModel { LocallyAvailableViewModel(db) }

    val subscriptionsPager = subscriptionsVm.subscriptions.collectAsLazyPagingItems()
    val continuePlayingPager = continuePlayingVm.continuePlaying.collectAsLazyPagingItems()
    val newEpisodesPager = newEpisodesVm.newEpisodes.collectAsLazyPagingItems()
    val locallyAvailablePager = locallyAvailableVm.locallyAvailable.collectAsLazyPagingItems()

    val loaded = remember { mutableStateOf(false) }
    LaunchedEffect(
        subscriptionsPager.loadState.isIdle,
        continuePlayingPager.loadState.isIdle,
        newEpisodesPager.loadState.isIdle,
        locallyAvailablePager.loadState.isIdle
    ) {
        if(!subscriptionsPager.loadState.isIdle) return@LaunchedEffect
        if(!continuePlayingPager.loadState.isIdle) return@LaunchedEffect
        if(!newEpisodesPager.loadState.isIdle) return@LaunchedEffect
        if(!locallyAvailablePager.loadState.isIdle) return@LaunchedEffect
        loaded.value = true
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val listState = rememberLazyListState()

    val expandedFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    BoxWithConstraints {
        Scaffold(
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            when(useAlternativeBranding.value) {
                                false -> stringResource(R.string.app_name)
                                true -> stringResource(R.string.app_name_alternative)
                            }
                        )
                    },
                    actions = {
                        FilledIconButton(
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            onClick = onSettings
                        ) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.route_settings)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                MediumExtendedFloatingActionButton(
                    modifier = Modifier.padding(
                        bottom = if(maxWidth <= FloatingMediaPlayerBreakpoint)
                            LocalFloatingMediaPlayerHeight.current
                        else
                            0.dp
                    ),
                    expanded = expandedFab,
                    text = {
                        Text(stringResource(R.string.common_action_add))
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.common_action_add),
                            modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                        )
                    },
                    onClick = onClickAddPodcast
                )
            }
        ) { inset ->
            Box(
                Modifier
                    .padding(inset)
                    .fillMaxSize()
            ) {
                PagerScaffold(
                    subscriptionsPager,
                    continuePlayingPager,
                    newEpisodesPager,
                    locallyAvailablePager,
                    isEmpty = {
                        InfoLayout(
                            icon = Icons.Rounded.Podcasts,
                            title = {
                                stringResource(R.string.route_home_empty_title)
                            },
                            content = {
                                Text(
                                    text = stringResource(R.string.route_home_empty_text),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(32.dp))

                                Button(
                                    onClick = {
                                        onClickDiscover()
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Explore,
                                        contentDescription = stringResource(R.string.route_discover),
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                    )

                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                                    Text(stringResource(R.string.route_discover))
                                }
                            }
                        )
                    }
                ) {
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        if(loaded.value) LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            if(subscriptionsPager.itemCount > 0) item(
                                key = "SUBSCRIPTIONS"
                            ) {
                                SectionCarousel(
                                    modifier = Modifier.animateItem(),
                                    title = stringResource(R.string.route_subscriptions),
                                    onClickExpand = { onClickSubscriptions() }
                                ) {
                                    items(subscriptionsPager.itemCount) {
                                        val bundle = subscriptionsPager[it]
                                            ?: return@items

                                        SubscriptionCard(
                                            modifier = Modifier.animateItem(),
                                            bundle = bundle,
                                            onClick = {
                                                onClickPodcast(bundle.podcast.origin)
                                            }
                                        )
                                    }
                                }
                            }

                            if(continuePlayingPager.itemCount > 0) item(
                                key = "CONTINUE_PLAYING"
                            ) {
                                Section(
                                    modifier = Modifier.animateItem(),
                                    title = stringResource(R.string.route_continue_playing),
                                    badge = {
                                        Badge {
                                            Text(
                                                text = continuePlayingPager.itemCount.toString()
                                            )
                                        }
                                    },
                                    onClickExpand = { onClickContinuePlaying() }
                                ) {
                                    val backgroundColor = MaterialTheme.colorScheme.background

                                    val count = continuePlayingPager.itemCount.coerceAtMost(2)

                                    Column(
                                        Modifier
                                            .padding(start = 16.dp, end = 16.dp)
                                            .drawWithContent {
                                                drawContent()

                                                if(continuePlayingPager.itemCount > 2) drawRect(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Transparent,
                                                            backgroundColor
                                                        )
                                                    )
                                                )
                                            },
                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                                    ) {
                                        repeat(count) {
                                            val item = continuePlayingPager[it] ?: return@repeat

                                            PodcastEpisodeListItem(
                                                bundle = item.toPodcastEpisodeBundle(),

                                                descriptionText = item.episode.podcastTitle,

                                                index = it,
                                                count = count,

                                                colors = ListItemDefaults.segmentedColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),

                                                onClick = {
                                                    onClickEpisode(item.episode)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if(newEpisodesPager.itemCount > 0) item(
                                key = "NEW_EPISODES"
                            ) {
                                Section(
                                    modifier = Modifier.animateItem(),
                                    title = stringResource(R.string.route_new_episodes),
                                    badge = {
                                        Badge {
                                            Text(
                                                text = newEpisodesPager.itemCount.toString()
                                            )
                                        }
                                    },
                                    onClickExpand = { onClickNewEpisodes() }
                                ) {
                                    val backgroundColor = MaterialTheme.colorScheme.background

                                    val count = newEpisodesPager.itemCount.coerceAtMost(2)

                                    Column(
                                        Modifier
                                            .padding(start = 16.dp, end = 16.dp)
                                            .drawWithContent {
                                                drawContent()

                                                if(newEpisodesPager.itemCount > 2) drawRect(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Transparent,
                                                            backgroundColor
                                                        )
                                                    )
                                                )
                                            },
                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                                    ) {
                                        repeat(count) {
                                            val item = newEpisodesPager[it] ?: return@repeat

                                            PodcastEpisodeListItem(
                                                bundle = item,

                                                descriptionText = item.episode.podcastTitle,

                                                index = it,
                                                count = count,

                                                colors = ListItemDefaults.segmentedColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),

                                                onClick = {
                                                    onClickEpisode(item.episode)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if(locallyAvailablePager.itemCount > 0) item(
                                key = "LOCALLY_AVAILABLE"
                            ) {
                                SectionCarousel(
                                    modifier = Modifier.animateItem(),
                                    title = stringResource(R.string.route_locally_available),
                                    onClickExpand = { onClickLocallyAvailable() }
                                ) {
                                    items(locallyAvailablePager.itemCount) {
                                        val podcast = locallyAvailablePager[it]
                                            ?: return@items

                                        PodcastCard(
                                            modifier = Modifier.animateItem(),
                                            podcast = podcast,
                                            onClick = {
                                                onClickPodcast(podcast.origin)
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                FloatingMediaPlayerSpacer()
                            }
                        }
                    }
                }
            }
        }
    }
}