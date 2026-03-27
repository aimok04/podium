package app.podiumpodcasts.podium.ui.route.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.PoweredByApplePodcastsBadge
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.component.layout.LoadingLayout
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.media.LocalFloatingMediaPlayerHeight
import app.podiumpodcasts.podium.ui.component.model.PodcastPreviewCard
import app.podiumpodcasts.podium.ui.dialog.CountryCodeSelectorDialog
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.PodcastPreviewBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.vm.discover.DiscoverViewModel
import app.podiumpodcasts.podium.ui.vm.discover.State
import app.podiumpodcasts.podium.ui.vm.discover.Topics
import app.podiumpodcasts.podium.utils.getCountryCode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverRoute(
    onClickPodcast: (origin: String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val settingsRepository = LocalSettingsRepository.current
    val disableApplePodcastsApi = settingsRepository.privacy.disableApplePodcastsApi
        .collectAsState(false)

    if(disableApplePodcastsApi.value) {
        Text("Discover isn't available")
        return
    }

    val defaultCountryCode = remember { getCountryCode(context) }
    val discoverCountryCode = settingsRepository.behavior.getDiscoverCountryCode(defaultCountryCode)
        .collectAsState(defaultCountryCode)

    val vm = viewModel<DiscoverViewModel>()

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val pagerState = rememberPagerState { Topics.entries.size }

    val showCountryCodeSelector = remember { mutableStateOf(false) }

    LaunchedEffect(discoverCountryCode.value) {
        vm.updateCountryCode(
            countryCode = discoverCountryCode.value,
            currentPage = pagerState.currentPage
        )
    }

    Scaffold(
        topBar = {
            DiscoverSearch(
                countryCode = discoverCountryCode.value,

                textFieldState = textFieldState,
                searchBarState = searchBarState,
                actions = {
                    IconButton(
                        onClick = {
                            showCountryCodeSelector.value = true
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Language,
                            contentDescription = stringResource(R.string.common_action_select_country_code)
                        )
                    }
                },

                onClickPodcast = onClickPodcast
            )
        }
    ) { inset ->
        Box(
            Modifier
                .padding(inset)
                .fillMaxSize()
        ) {
            Column {
                val selectedTopic = Topics.entries[pagerState.currentPage]

                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTopic.ordinal,
                    edgePadding = 16.dp,
                    minTabWidth = 60.dp
                ) {
                    Topics.entries.forEachIndexed { index, item ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = stringResource(item.label),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState
                ) { index ->
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        LaunchedEffect(index) {
                            vm.updatePage(
                                countryCode = discoverCountryCode.value,
                                index = index
                            )
                        }

                        AnimatedContent(
                            targetState = vm.states[index]
                        ) { state ->
                            when(state) {
                                is State.Loading -> {
                                    LoadingLayout()
                                }

                                is State.Done -> {
                                    LazyVerticalGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        columns = GridCells.Adaptive(100.dp),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        items(
                                            count = state.result.size,
                                            key = { state.result[it].fetchUrl }
                                        ) {
                                            val item = state.result[it]

                                            PodcastPreviewCard(
                                                podcast = item,
                                                onClick = {
                                                    vm.clickPodcastPreview(item)
                                                }
                                            )
                                        }

                                        item(
                                            span = { GridItemSpan(maxLineSpan) }
                                        ) {
                                            FloatingMediaPlayerSpacer(64.dp)
                                        }
                                    }
                                }

                                is State.Error -> {
                                    ErrorLayout {
                                        Text(state.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = LocalFloatingMediaPlayerHeight.current)
            ) {
                PoweredByApplePodcastsBadge()
            }
        }
    }

    PodcastPreviewBottomSheet(
        state = vm.previewBottomSheetState,
        onOpenPodcast = {
            onClickPodcast(it.origin)
        }
    )

    if(showCountryCodeSelector.value) CountryCodeSelectorDialog(
        value = discoverCountryCode.value,
        onValueChange = {
            scope.launch {
                settingsRepository.behavior.setDiscoverCountryCode(it)
            }
        },
        onDismiss = {
            showCountryCodeSelector.value = false
        }
    )
}