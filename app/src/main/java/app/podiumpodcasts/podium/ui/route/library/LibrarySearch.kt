package app.podiumpodcasts.podium.ui.route.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.component.common.CommonSearchInputField
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.model.PodcastCard
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.library.LibrarySearchState
import app.podiumpodcasts.podium.ui.vm.library.LibrarySearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibrarySearch(
    scrollBehavior: SearchBarScrollBehavior,

    searchBarState: SearchBarState = rememberSearchBarState(),
    textFieldState: TextFieldState = rememberTextFieldState(),

    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,

    onClickPodcast: (origin: String) -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    val scope = rememberCoroutineScope()

    val db = LocalDatabase.current
    val vm = viewModel { LibrarySearchViewModel(db) }

    fun dismiss(
        after: () -> Unit
    ) {
        scope.launch {
            searchBarState.animateToCollapsed()
            after()
        }
    }

    AppBarWithSearch(
        state = searchBarState,
        colors = SearchBarDefaults.appBarWithSearchColors(
            scrolledAppBarContainerColor = MaterialTheme.colorScheme.surface
        ),
        inputField = {
            CommonSearchInputField(
                enabled = searchBarState.currentValue == SearchBarValue.Expanded,
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { vm.search(it) }
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior
    )

    ExpandedFullScreenContainedSearchBar(
        state = searchBarState,
        inputField = {
            CommonSearchInputField(
                enabled = searchBarState.currentValue == SearchBarValue.Expanded,
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { vm.search(it) }
            )
        }
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            SearchContent(
                vm = vm,
                onClickPodcast = {
                    dismiss {
                        onClickPodcast(it)
                    }
                },
                onClickEpisode = {
                    dismiss {
                        onClickEpisode(it)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchContent(
    vm: LibrarySearchViewModel,
    onClickPodcast: (origin: String) -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    AnimatedContent(
        targetState = vm.state.value
    ) { state ->
        when(state) {
            is LibrarySearchState.Idle -> {
                InfoLayout(
                    icon = Icons.Rounded.Search,
                    title = { stringResource(R.string.route_library_search_title) }
                ) {
                    Text(
                        text = stringResource(R.string.route_library_search_text),
                        textAlign = TextAlign.Center
                    )
                }
            }

            is LibrarySearchState.Done -> {
                val podcastsPager = state.podcastsPager.collectAsLazyPagingItems()
                val episodesPager = state.episodesPager.collectAsLazyPagingItems()

                val isEmpty = (podcastsPager.itemCount == 0 && episodesPager.itemCount == 0)
                        && (podcastsPager.loadState.isIdle && episodesPager.loadState.isIdle)

                AnimatedContent(
                    targetState = isEmpty
                ) { isEmpty ->
                    when(isEmpty) {
                        true -> {
                            InfoLayout(
                                icon = Icons.Rounded.SearchOff,
                                title = { stringResource(R.string.route_library_search_empty_title) },
                            ) {
                                Text(
                                    text = stringResource(R.string.route_library_search_empty_text),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                            ) {
                                if(podcastsPager.itemCount > 0) {
                                    item(
                                        key = "PODCASTS"
                                    ) {
                                        LazyRow(
                                            Modifier.animateItem(),
                                            contentPadding = PaddingValues(
                                                start = 16.dp,
                                                end = 16.dp
                                            )
                                        ) {
                                            items(
                                                podcastsPager.itemCount
                                            ) {
                                                podcastsPager[it]?.let { podcast ->
                                                    PodcastCard(
                                                        podcast = podcast,
                                                        onClick = { onClickPodcast(podcast.origin) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Spacer(Modifier.height(16.dp))
                                    }
                                }

                                items(
                                    count = episodesPager.itemCount,
                                    key = { episodesPager[it]!!.episode.id }
                                ) { index ->
                                    episodesPager[index]?.let { bundle ->
                                        PodcastEpisodeListItem(
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp)
                                                .animateItem(),

                                            bundle = bundle,

                                            index = index,
                                            count = episodesPager.itemCount,

                                            onClick = { onClickEpisode(bundle.episode) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}