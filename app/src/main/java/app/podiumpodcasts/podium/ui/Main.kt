package app.podiumpodcasts.podium.ui

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import app.podiumpodcasts.podium.ui.component.common.SwitchableDynamicMaterialExpressiveTheme
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayer
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerHeight
import app.podiumpodcasts.podium.ui.component.media.LocalFloatingMediaPlayerHeight
import app.podiumpodcasts.podium.ui.component.media.LocalFloatingMediaPlayerShown
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.media.MediaPlayerBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.navigation.Home
import app.podiumpodcasts.podium.ui.navigation.NavBarScaffold
import app.podiumpodcasts.podium.ui.navigation.Navigation
import app.podiumpodcasts.podium.ui.navigation.PodiumNavKey
import app.podiumpodcasts.podium.ui.view.model.PodcastDetailView
import app.podiumpodcasts.podium.ui.view.model.PodcastEpisodeDetailView
import app.podiumpodcasts.podium.ui.vm.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
open class DetailPaneKey : Parcelable {
    @Serializable
    open class PodcastKey(
        val origin: String
    ) : DetailPaneKey()

    @Serializable
    class PodcastEpisodeKey(
        val episodeOrigin: String,
        val episodeId: String
    ) : PodcastKey(episodeOrigin)

    @Serializable
    class EpisodeKey(
        val episodeOrigin: String,
        val episodeId: String
    ) : DetailPaneKey()
}

val enterSpec = fadeIn(animationSpec = tween(220, delayMillis = 90)) +
        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))

val exitSpec = fadeOut(animationSpec = tween(90))


@OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun Main(
    deepLink: DeepLink?
) {
    val scope = rememberCoroutineScope()

    val db = LocalDatabase.current
    val vm = viewModel(key = "MAIN") {
        MainViewModel(
            db = db,
            defaultShowMediaPlayerBottomSheet = false
        )
    }

    val backStack = rememberNavBackStack(deepLink?.route ?: Home)

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator<DetailPaneKey>()

    val navigationScaffoldState = rememberNavigationSuiteScaffoldState()
    val navigationScaffoldLayoutType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

    fun listDetailBack() {
        scope.launch {
            if(!listDetailNavigator.canNavigateBack(BackNavigationBehavior.PopUntilScaffoldValueChange)) return@launch
            listDetailNavigator.navigateBack(
                BackNavigationBehavior.PopUntilScaffoldValueChange
            )
        }
    }

    LaunchedEffect(Unit) {
        if(deepLink?.detailPaneKey != null)
            listDetailNavigator.navigateTo(ThreePaneScaffoldRole.Primary, deepLink.detailPaneKey)

        if(deepLink?.showMediaPlayerBottomSheet == true) {
            delay(500)
            vm.showMediaPlayerBottomSheet.value = true
        }
    }

    LaunchedEffect(backStack.last(), listDetailNavigator.scaffoldValue.primary) {
        if(navigationScaffoldLayoutType != NavigationSuiteType.NavigationBar) return@LaunchedEffect

        val entry = backStack.last()
        if(entry is PodiumNavKey) {
            vm.hideFloatingMediaPlayer.value = !entry.showMediaPlayer

            val shouldNavBeVisible =
                entry.showNavBar && listDetailNavigator.scaffoldValue.primary == PaneAdaptedValue.Hidden

            val isNavVisible =
                navigationScaffoldState.currentValue == NavigationSuiteScaffoldValue.Visible
            if(shouldNavBeVisible == isNavVisible) return@LaunchedEffect

            if(shouldNavBeVisible) {
                navigationScaffoldState.show()
            } else {
                navigationScaffoldState.hide()
            }
        }
    }

    val floatingMediaPlayerShown = remember { mutableStateOf(false) }
    val floatingMediaPlayerHeightAnimation = remember { Animatable(0f) }

    NavBarScaffold(
        layoutType = navigationScaffoldLayoutType,
        state = navigationScaffoldState,
        onClickItem = {
            backStack.add(it.navKey)
            listDetailBack()
        },
        currentNavKey = { backStack.last() }
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingMediaPlayer(
                    hide = vm.hideFloatingMediaPlayer.value || navigationScaffoldState.isAnimating,
                    showMediaPlayerBottomSheet = vm.showMediaPlayerBottomSheet.value,
                    onMediaPlayerShownChange = {
                        floatingMediaPlayerShown.value = it

                        scope.launch {
                            floatingMediaPlayerHeightAnimation.animateTo(
                                if(it)
                                    1f
                                else
                                    0f
                            )
                        }
                    },
                    onClick = {
                        vm.showMediaPlayerBottomSheet.value = true
                    }
                )
            }
        ) {
            CompositionLocalProvider(
                LocalFloatingMediaPlayerHeight provides (FloatingMediaPlayerHeight + 16.dp)
                        * floatingMediaPlayerHeightAnimation.value,
                LocalFloatingMediaPlayerShown provides floatingMediaPlayerShown.value
            ) {
                NavigableListDetailPaneScaffold(
                    defaultBackBehavior = BackNavigationBehavior.PopLatest,
                    navigator = listDetailNavigator,
                    listPane = {
                        AnimatedPane(
                            enterTransition = enterSpec,
                            exitTransition = exitSpec
                        ) {
                            Navigation(
                                backStack = backStack,

                                onBack = {
                                    if(listDetailNavigator.canNavigateBack(BackNavigationBehavior.PopUntilScaffoldValueChange)) {
                                        listDetailBack()
                                    } else {
                                        backStack.removeLastOrNull()
                                    }
                                },

                                onClickPodcast = { origin ->
                                    scope.launch {
                                        listDetailNavigator.navigateTo(
                                            pane = ThreePaneScaffoldRole.Primary,
                                            contentKey = DetailPaneKey.PodcastKey(
                                                origin = origin
                                            )
                                        )
                                    }
                                },
                                onClickEpisode = { episode ->
                                    scope.launch {
                                        listDetailNavigator.navigateTo(
                                            pane = ThreePaneScaffoldRole.Primary,
                                            contentKey = DetailPaneKey.EpisodeKey(
                                                episodeOrigin = episode.origin,
                                                episodeId = episode.id
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    },
                    detailPane = {
                        DetailPane(
                            vm = vm,
                            scaffoldNavigator = listDetailNavigator
                        )
                    },
                    paneExpansionState = rememberPaneExpansionState(
                        keyProvider = listDetailNavigator.scaffoldValue,
                        anchors = listOf(
                            PaneExpansionAnchor.Proportion(0f),
                            PaneExpansionAnchor.Proportion(0.3f),
                            PaneExpansionAnchor.Proportion(0.5f),
                            PaneExpansionAnchor.Proportion(0.7f),
                        )
                    ),
                    paneExpansionDragHandle = { state ->
                        val interactionSource = remember { MutableInteractionSource() }
                        VerticalDragHandle(
                            modifier = Modifier.paneExpansionDraggable(
                                state,
                                LocalMinimumInteractiveComponentSize.current,
                                interactionSource
                            ),
                            interactionSource = interactionSource
                        )
                    }
                )
            }
        }

        if(vm.showMediaPlayerBottomSheet.value) MediaPlayerBottomSheet(
            onOpenEpisode = { origin, id ->
                vm.showMediaPlayerBottomSheet.value = false

                scope.launch {
                    listDetailNavigator.navigateTo(
                        pane = ThreePaneScaffoldRole.Primary,
                        contentKey = DetailPaneKey.EpisodeKey(
                            episodeOrigin = origin,
                            episodeId = id
                        )
                    )
                }
            },
            onDismiss = { vm.showMediaPlayerBottomSheet.value = false }
        )
    }
}

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun ThreePaneScaffoldPaneScope.DetailPane(
    vm: MainViewModel,
    scaffoldNavigator: ThreePaneScaffoldNavigator<DetailPaneKey>
) {
    val settingsRepository = LocalSettingsRepository.current

    val enableArtworkColors = settingsRepository.appearance.enableArtworkColors
        .collectAsState(true)

    val scope = rememberCoroutineScope()

    scaffoldNavigator.currentDestination?.contentKey?.let { contentKey ->
        if(contentKey is DetailPaneKey.PodcastKey) {
            val podcast = vm.fetchPodcast(contentKey.origin)
                .collectAsState(null)

            podcast.value?.let { podcast ->
                AnimatedPane(
                    enterTransition = enterSpec,
                    exitTransition = exitSpec
                ) {
                    SwitchableDynamicMaterialExpressiveTheme(
                        enable = enableArtworkColors.value,
                        seedColor = Color(podcast.imageSeedColor)
                    ) {
                        val displayBundle = remember { mutableStateOf(false) }
                        val episodeId = remember { mutableStateOf<String?>(null) }

                        val bundle = episodeId.value?.let {
                            vm.fetchEpisode(it)
                                .collectAsState(null)
                        }

                        LaunchedEffect(contentKey, bundle?.value) {
                            if(contentKey is DetailPaneKey.PodcastEpisodeKey)
                                episodeId.value = contentKey.episodeId

                            displayBundle.value = bundle?.value != null
                                    && contentKey is DetailPaneKey.PodcastEpisodeKey
                        }

                        AnimatedContent(
                            targetState = displayBundle.value
                        ) { isBundle ->
                            when(isBundle) {
                                false -> PodcastDetailView(
                                    podcast = podcast,
                                    onBack = {
                                        scope.launch {
                                            scaffoldNavigator.navigateBack(
                                                backNavigationBehavior = BackNavigationBehavior.PopLatest
                                            )
                                        }
                                    },
                                    onClickEpisode = { episode ->
                                        scope.launch {
                                            scaffoldNavigator.navigateTo(
                                                pane = ThreePaneScaffoldRole.Primary,
                                                contentKey = DetailPaneKey.PodcastEpisodeKey(
                                                    episodeOrigin = podcast.origin,
                                                    episodeId = episode.id
                                                )
                                            )
                                        }
                                    }
                                )

                                true -> PodcastEpisodeDetailView(
                                    bundle = bundle!!.value!!,
                                    parent = podcast,
                                    onBack = {
                                        scope.launch {
                                            scaffoldNavigator.navigateBack(
                                                backNavigationBehavior = BackNavigationBehavior.PopLatest
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else if(contentKey is DetailPaneKey.EpisodeKey) {
            val bundle = vm.fetchEpisode(contentKey.episodeId)
                .collectAsState(null)

            val podcast = vm.fetchPodcast(contentKey.episodeOrigin)
                .collectAsState(null)

            bundle.value?.let { bundle ->
                AnimatedPane(
                    enterTransition = enterSpec,
                    exitTransition = exitSpec
                ) {
                    SwitchableDynamicMaterialExpressiveTheme(
                        enable = enableArtworkColors.value,
                        seedColor = Color(bundle.episode.imageSeedColor)
                    ) {
                        PodcastEpisodeDetailView(
                            bundle = bundle,
                            parent = podcast.value,
                            showParentLink = true,
                            onShowParent = {
                                scope.launch {
                                    scaffoldNavigator.navigateTo(
                                        pane = ThreePaneScaffoldRole.Primary,
                                        contentKey = DetailPaneKey.PodcastKey(
                                            origin = contentKey.episodeOrigin
                                        )
                                    )
                                }
                            },
                            onBack = {
                                scope.launch {
                                    scaffoldNavigator.navigateBack(
                                        backNavigationBehavior = BackNavigationBehavior.PopLatest
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}