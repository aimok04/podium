package app.podiumpodcasts.podium.ui.route.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.CommonSearchInputField
import app.podiumpodcasts.podium.ui.component.common.PoweredByApplePodcastsBadge
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.layout.LoadingLayout
import app.podiumpodcasts.podium.ui.component.model.PodcastPreviewCard
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.PodcastPreviewBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.discover.DiscoverSearchState
import app.podiumpodcasts.podium.ui.vm.discover.DiscoverSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverSearch(
    countryCode: String,

    searchBarState: SearchBarState = rememberSearchBarState(),
    textFieldState: TextFieldState = rememberTextFieldState(),
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,

    onClickPodcast: (origin: String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val db = LocalDatabase.current

    val vm = viewModel(key = countryCode) { DiscoverSearchViewModel(countryCode, db) }

    val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
    )

    AppBarWithSearch(
        state = searchBarState,
        colors = appBarWithSearchColors,
        inputField = {
            CommonSearchInputField(
                enabled = searchBarState.currentValue == SearchBarValue.Expanded,
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { vm.search(it) }
            )
        },
        navigationIcon = navigationIcon,
        actions = actions
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
        },
        colors = appBarWithSearchColors.searchBarColors,
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            SearchContent(
                vm = vm
            )
        }
    }

    PodcastPreviewBottomSheet(
        state = vm.podcastPreviewBottomSheetState,
        onOpenPodcast = {
            scope.launch {
                searchBarState.animateToCollapsed()
                onClickPodcast(it.origin)
            }
        }
    )
}

@Composable
private fun BoxScope.SearchContent(
    vm: DiscoverSearchViewModel
) {
    AnimatedContent(
        targetState = vm.state.value
    ) { state ->
        when(state) {
            is DiscoverSearchState.Idle -> {
                InfoLayout(
                    icon = Icons.Rounded.Explore,
                    title = { stringResource(R.string.route_discover_search_title) }
                ) {
                    Text(
                        text = stringResource(R.string.route_discover_search_text),
                        textAlign = TextAlign.Center
                    )
                }
            }

            is DiscoverSearchState.Loading -> {
                LoadingLayout()
            }

            is DiscoverSearchState.Empty -> {
                InfoLayout(
                    icon = Icons.Rounded.SearchOff,
                    title = { stringResource(R.string.route_discover_search_empty_title) }
                ) {
                    Text(
                        text = stringResource(R.string.route_discover_search_empty_text),
                        textAlign = TextAlign.Center
                    )
                }
            }

            is DiscoverSearchState.Done -> {
                LazyVerticalGrid(
                    state = vm.gridState,
                    columns = GridCells.Adaptive(100.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(state.result.size) {
                        val podcast = state.result[it]

                        PodcastPreviewCard(
                            modifier = Modifier.fillMaxWidth(),
                            podcast = podcast,
                            onClick = {
                                vm.podcastPreviewBottomSheetState.show(podcast)
                            }
                        )
                    }

                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Spacer(Modifier.height(64.dp))
                    }
                }
            }

            is DiscoverSearchState.Error -> {
                ErrorLayout {
                    Text(state.error)
                }
            }
        }
    }

    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {
        PoweredByApplePodcastsBadge()
    }
}