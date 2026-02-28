package app.podiumpodcasts.podium.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.ui.route.add.AddPodcastRoute
import app.podiumpodcasts.podium.ui.route.content.ContinuePlayingRoute
import app.podiumpodcasts.podium.ui.route.content.LocallyAvailableRoute
import app.podiumpodcasts.podium.ui.route.content.NewEpisodesRoute
import app.podiumpodcasts.podium.ui.route.content.SubscriptionsRoute
import app.podiumpodcasts.podium.ui.route.discover.DiscoverRoute
import app.podiumpodcasts.podium.ui.route.downloads.DownloadsRoute
import app.podiumpodcasts.podium.ui.route.history.HistoryRoute
import app.podiumpodcasts.podium.ui.route.home.HomeRoute
import app.podiumpodcasts.podium.ui.route.library.LibraryRoute
import app.podiumpodcasts.podium.ui.route.licenses.LicensesRoute
import app.podiumpodcasts.podium.ui.route.list.ListRoute
import app.podiumpodcasts.podium.ui.route.restore.RestoreRoute
import app.podiumpodcasts.podium.ui.route.settings.SettingsRoute
import kotlinx.serialization.Serializable

// Routes
@Serializable
open class PodiumNavKey(
    val showNavBar: Boolean = true,
    val showMediaPlayer: Boolean = true
) : NavKey

@Serializable
data object Home : PodiumNavKey()
@Serializable
data object Discover : PodiumNavKey()
@Serializable
data object Library : PodiumNavKey()

@Serializable
data object History : PodiumNavKey(showNavBar = false)
@Serializable
data object Downloads : PodiumNavKey(showNavBar = false)
@Serializable
data object Subscriptions : PodiumNavKey(showNavBar = false)
@Serializable
data object ContinuePlaying : PodiumNavKey(showNavBar = false)
@Serializable
data object NewEpisodes : PodiumNavKey(showNavBar = false)
@Serializable
data object LocallyAvailable : PodiumNavKey(showNavBar = false)

@Serializable
data class List(val listId: Int) : PodiumNavKey(showNavBar = false)

@Serializable
data object AddPodcast : PodiumNavKey(showNavBar = false, showMediaPlayer = false)
@Serializable
data object Settings : PodiumNavKey(showNavBar = false, showMediaPlayer = false)
@Serializable
data object Licenses : PodiumNavKey(showNavBar = false, showMediaPlayer = false)
@Serializable
data object Restore : PodiumNavKey(showNavBar = false, showMediaPlayer = false)
@Serializable
data object Unknown : PodiumNavKey()

@Composable
fun Navigation(
    backStack: NavBackStack<NavKey>,

    onBack: () -> Unit = { },

    onClickPodcast: (origin: String) -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        popTransitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        predictivePopTransitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        entryProvider = { key ->
            when(key) {
                is Home -> NavEntry(key) {
                    HomeRoute(
                        onSettings = { backStack.add(Settings) },

                        onClickSubscriptions = { backStack.add(Subscriptions) },
                        onClickContinuePlaying = { backStack.add(ContinuePlaying) },
                        onClickNewEpisodes = { backStack.add(NewEpisodes) },
                        onClickLocallyAvailable = { backStack.add(LocallyAvailable) },

                        onClickAddPodcast = { backStack.add(AddPodcast) },

                        onClickDiscover = { backStack.add(Discover) },

                        onClickPodcast = onClickPodcast,
                        onClickEpisode = onClickEpisode
                    )
                }

                is Discover -> NavEntry(key) {
                    DiscoverRoute(
                        onClickPodcast = { podcast -> onClickPodcast(podcast) }
                    )
                }

                is Library -> NavEntry(key) {
                    LibraryRoute(
                        onClickDownloads = { backStack.add(Downloads) },
                        onClickHistory = { backStack.add(History) },
                        onClickContinuePlaying = { backStack.add(ContinuePlaying) },
                        onClickNewEpisodes = { backStack.add(NewEpisodes) },

                        onClickPodcast = onClickPodcast,
                        onClickEpisode = onClickEpisode,

                        onClickList = {
                            backStack.add(List(it))
                        }
                    )
                }

                is History -> NavEntry(key) {
                    HistoryRoute(
                        onClickEpisode = onClickEpisode
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is Downloads -> NavEntry(key) {
                    DownloadsRoute(
                        onClickEpisode = onClickEpisode
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is Subscriptions -> NavEntry(key) {
                    SubscriptionsRoute(
                        onClickPodcast = onClickPodcast
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is ContinuePlaying -> NavEntry(key) {
                    ContinuePlayingRoute(
                        onClickEpisode = onClickEpisode
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is NewEpisodes -> NavEntry(key) {
                    NewEpisodesRoute(
                        onClickEpisode = onClickEpisode
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is LocallyAvailable -> NavEntry(key) {
                    LocallyAvailableRoute(
                        onClickPodcast = onClickPodcast
                    ) {
                        backStack.removeLastOrNull()
                    }
                }


                is List -> NavEntry(key) {
                    ListRoute(
                        listId = key.listId,
                        onClickPodcast = onClickPodcast,
                        onClickEpisode = onClickEpisode
                    ) {
                        backStack.removeLastOrNull()
                    }
                }


                is AddPodcast -> NavEntry(key) {
                    AddPodcastRoute(
                        onOpenPodcast = onClickPodcast
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is Settings -> NavEntry(key) {
                    SettingsRoute(
                        onLicenses = {
                            backStack.add(Licenses)
                        },
                        onRestore = {
                            backStack.clear()
                            backStack.add(Restore)
                        }
                    ) {
                        backStack.removeLastOrNull()
                    }
                }

                is Licenses -> NavEntry(key) {
                    LicensesRoute {
                        backStack.removeLastOrNull()
                    }
                }

                is Restore -> NavEntry(key) {
                    RestoreRoute()
                }


                else -> NavEntry(Unknown) {
                    Text("Unknown route")
                }
            }
        }
    )
}