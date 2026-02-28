package app.podiumpodcasts.podium.ui.view.model

import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileDownloadOff
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.component.DetailsList
import app.podiumpodcasts.podium.ui.component.DetailsListItemModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.common.BubbleButton
import app.podiumpodcasts.podium.ui.component.common.ButtonLabelWithIconInset
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItem
import app.podiumpodcasts.podium.ui.component.common.swipeable.SwipeableItemActions
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.ContentFavoriteButton
import app.podiumpodcasts.podium.ui.component.model.ContentSaveToListButton
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeListItem
import app.podiumpodcasts.podium.ui.dialog.DeleteConfirmationDialog
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.PodcastSettingsBottomSheet
import app.podiumpodcasts.podium.ui.formatFileSize
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.PodcastDetailViewModel
import com.materialkolor.ktx.harmonizeWithPrimary
import dev.chrisbanes.haze.hazeEffect

enum class Destinations(
    val index: Int,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    EPISODES(0, R.string.common_episodes, Icons.AutoMirrored.Filled.QueueMusic),
    INFO(1, R.string.common_info, Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailView(
    podcast: PodcastModel,
    onBack: () -> Unit,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    val db = LocalDatabase.current
    val activity = LocalActivity.current as AppActivity

    val vm = viewModel<PodcastDetailViewModel>(key = podcast.origin)

    val subscription = vm.subscription(db, podcast).collectAsState(null)
    val episodeBundleList = vm.episodeBundleList(db, podcast).collectAsState(listOf())

    val isSubscribed = subscription.value != null
    val enableNotifications = subscription.value?.enableNotifications == true
    val enableAutoDownload = subscription.value?.enableAutoDownload == true

    val BACKDROP_SIZE = 200.dp

    PullToRefreshBox(
        isRefreshing = vm.isRefreshing,
        onRefresh = { vm.updatePodcast(db, activity, podcast) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        BackButton {
                            onBack()
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    title = { },
                    actions = {
                        BubbleButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.route_podcast_settings),
                            onClick = {
                                vm.showSettingsBottomSheet.value = true
                            }
                        )
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { inset ->
            Box {
                ShimmerAsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(inset.calculateTopPadding() + BACKDROP_SIZE)
                        .hazeEffect(),

                    model = podcast.imageUrl,
                    contentDescription = null,

                    contentScale = ContentScale.Crop
                )

                LazyColumn(
                    state = if(episodeBundleList.value.isNotEmpty())
                        vm.lazyListState
                    else
                        rememberLazyListState()
                ) {
                    item(
                        key = "BACKDROP"
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(inset.calculateTopPadding() + BACKDROP_SIZE)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    )
                                )
                        ) {
                            ShimmerAsyncImage(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(128.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        clip = true
                                    ),

                                model = podcast.imageUrl,
                                contentDescription = null,

                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item(
                        key = "HEADING"
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column {
                                Spacer(Modifier.height(16.dp))

                                SelectionContainer(
                                    Modifier
                                        .padding(start = 16.dp, end = 16.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = podcast.fetchTitle(),
                                            style = Typography.displaySmallEmphasized
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = podcast.author,
                                            style = Typography.labelLarge
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    item(
                        key = "BUTTON_ROW"
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                Modifier.horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.width(16.dp))

                                ToggleButton(
                                    checked = isSubscribed,
                                    onCheckedChange = {
                                        if(!isSubscribed) {
                                            vm.subscribe(db, podcast)
                                        } else {
                                            vm.unsubscribe(db, podcast)
                                        }
                                    },

                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    AnimatedContent(isSubscribed) { isSubscribed ->
                                        ButtonLabelWithIconInset(
                                            icon = when(isSubscribed) {
                                                true -> Icons.Rounded.Favorite
                                                false -> Icons.Rounded.FavoriteBorder
                                            },
                                            label = when(isSubscribed) {
                                                true -> stringResource(R.string.common_action_unsubscribe)
                                                false -> stringResource(R.string.common_action_subscribe)
                                            }
                                        )
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                AnimatedVisibility(
                                    visible = isSubscribed,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Row {
                                        FilledIconToggleButton(
                                            checked = enableNotifications,
                                            onCheckedChange = {
                                                when(enableNotifications) {
                                                    true -> vm.disableNotifications(db, podcast)
                                                    false -> vm.enableNotifications(db, podcast)
                                                }
                                            },

                                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                                checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            AnimatedContent(enableNotifications) { isEnabled ->
                                                Icon(
                                                    imageVector = when(isEnabled) {
                                                        true -> Icons.Rounded.NotificationsActive
                                                        false -> Icons.Rounded.NotificationsNone
                                                    },
                                                    contentDescription = when(isEnabled) {
                                                        true -> stringResource(R.string.common_downloading)
                                                        false -> stringResource(R.string.common_action_download)
                                                    }
                                                )
                                            }
                                        }

                                        FilledIconToggleButton(
                                            checked = enableAutoDownload,
                                            onCheckedChange = {
                                                when(enableAutoDownload) {
                                                    true -> vm.disableAutoDownload(db, podcast)
                                                    false -> vm.enableAutoDownload(db, podcast)
                                                }
                                            },

                                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                                checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            AnimatedContent(enableAutoDownload) { isEnabled ->
                                                Icon(
                                                    imageVector = when(isEnabled) {
                                                        true -> Icons.Rounded.FileDownloadOff
                                                        false -> Icons.Rounded.FileDownload
                                                    },
                                                    contentDescription = when(isEnabled) {
                                                        true -> stringResource(R.string.common_action_disable_auto_download)
                                                        false -> stringResource(R.string.common_action_enable_auto_download)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                VerticalDivider(
                                    Modifier.height(24.dp)
                                )

                                Spacer(Modifier.width(16.dp))

                                ContentSaveToListButton(
                                    contentId = podcast.origin,
                                    isPodcast = true
                                )

                                Spacer(Modifier.width(8.dp))

                                ContentFavoriteButton(
                                    contentId = podcast.origin,
                                    isPodcast = true
                                )

                                Spacer(Modifier.width(8.dp))

                                FilledIconButton(
                                    onClick = {
                                        vm.showDeleteDialog.value = true
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.harmonizeWithPrimary(
                                            MaterialTheme.colorScheme.errorContainer
                                        ),
                                        contentColor = MaterialTheme.colorScheme.harmonizeWithPrimary(
                                            MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = stringResource(R.string.common_action_delete)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))
                            }
                        }
                    }

                    item(
                        key = "SPACER_TAB_ROW"
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    item(
                        key = "TAB_ROW"
                    ) {
                        PrimaryTabRow(
                            selectedTabIndex = vm.selectedDestination.index,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Destinations.entries.forEach {
                                Tab(
                                    selected = vm.selectedDestination == it,
                                    onClick = { vm.selectedDestination = it },
                                    text = {
                                        Text(
                                            text = stringResource(it.label),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = it.icon,
                                            contentDescription = stringResource(it.label)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if(vm.selectedDestination == Destinations.INFO) {
                        podcastDetailViewInfoDestination(
                            podcast = podcast
                        )
                    } else if(vm.selectedDestination == Destinations.EPISODES) {
                        podcastDetailViewEpisodesDestination(
                            episodeBundleList = episodeBundleList.value,
                            onClickEpisode = onClickEpisode
                        )
                    }

                    item {
                        FloatingMediaPlayerSpacer()
                    }
                }
            }
        }
    }

    if(vm.showSettingsBottomSheet.value) PodcastSettingsBottomSheet(
        onDismiss = {
            vm.showSettingsBottomSheet.value = false
        },
        podcast = podcast
    )

    if(vm.showDeleteDialog.value) DeleteConfirmationDialog(
        onDismiss = {
            vm.showDeleteDialog.value = false
        },
        itemName = podcast.fetchTitle(),
        additionalText = stringResource(R.string.dialog_delete_confirmation_podcast),
        onConfirm = {
            vm.deletePodcast(db, podcast)
            onBack()
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.podcastDetailViewEpisodesDestination(
    episodeBundleList: List<PodcastEpisodeBundle>,
    onClickEpisode: (episode: PodcastEpisodeModel) -> Unit
) {
    item(
        key = "SPACER_EPISODES"
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Spacer(Modifier.height(16.dp))
        }
    }

    items(
        count = episodeBundleList.size,
        key = {
            "EPISODE:" + episodeBundleList[it].episode.id
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            val episodeBundle = episodeBundleList[it]

            Box(
                Modifier.padding(
                    top = 2.dp,

                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                SwipeableItem(
                    startAction = SwipeableItemActions.HearLaterAction(
                        episodeId = episodeBundle.episode.id
                    )
                ) {
                    PodcastEpisodeListItem(
                        bundle = episodeBundle,
                        index = it,
                        count = episodeBundleList.size,
                        onClick = {
                            onClickEpisode(episodeBundle.episode)
                        }
                    )
                }
            }
        }
    }

    item {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.podcastDetailViewInfoDestination(
    podcast: PodcastModel
) {
    item(
        key = "INFO"
    ) {
        val uriHandler = LocalUriHandler.current

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = AnnotatedString.fromHtml(
                                htmlString = podcast.description
                                    .replace("\n", "<br>")
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                DetailsList(
                    items = listOf(
                        DetailsListItemModel(
                            icon = Icons.Rounded.Link,
                            label = R.string.common_source,
                            value = podcast.link,
                            onClick = {
                                uriHandler.openUri(podcast.link)
                            }
                        ),
                        DetailsListItemModel(
                            icon = Icons.Rounded.RssFeed,
                            label = R.string.common_rss_feed,
                            value = podcast.origin
                        ),
                        DetailsListItemModel(
                            icon = Icons.Rounded.Language,
                            label = R.string.common_language_code,
                            value = podcast.languageCode
                        ),
                        DetailsListItemModel(
                            icon = Icons.Rounded.DataUsage,
                            label = R.string.common_data_usage_per_update,
                            value = formatFileSize(podcast.fileSize)
                        )
                    )
                )
            }
        }
    }
}