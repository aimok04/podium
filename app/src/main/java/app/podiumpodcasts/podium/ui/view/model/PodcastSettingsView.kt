package app.podiumpodcasts.podium.ui.view.model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.settings.SettingsHeader
import app.podiumpodcasts.podium.ui.component.settings.SettingsListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSecondsSliderListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.PodcastSettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastSettingsView(
    podcast: PodcastModel,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current

    val vm = viewModel<PodcastSettingsViewModel>()

    val subscription = remember { db.podcastSubscriptions().get(podcast.origin) }
        .collectAsState(null)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton(
                        icon = Icons.Rounded.Close,
                        onClick = onBack
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.route_podcast_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { inset ->
        LazyColumn(
            modifier = Modifier
                .padding(inset)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            subscription.value?.let { subscription ->
                item {
                    SettingsHeader(
                        label = stringResource(R.string.view_podcast_settings_details)
                    )
                }

                item {
                    val overrideTitle = remember { mutableStateOf("") }
                    LaunchedEffect(podcast.title, podcast.overrideTitle) {
                        overrideTitle.value = podcast.overrideTitle.ifBlank { podcast.title }
                    }

                    val isSame = overrideTitle.value == podcast.title

                    LaunchedEffect(overrideTitle.value) {
                        delay(1500)
                        vm.setOverrideTitle(
                            db, podcast, when(isSame) {
                                true -> ""
                                false -> overrideTitle.value
                            }
                        )
                    }

                    SettingsListItem(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Rounded.Label,
                                stringResource(R.string.view_podcast_settings_details_override_title)
                            )
                        },
                        label = stringResource(R.string.view_podcast_settings_details_override_title),

                        supportingContent = {
                            Column {
                                Text(stringResource(R.string.view_podcast_settings_details_override_title_description))

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        modifier = Modifier.weight(1f, true),

                                        value = overrideTitle.value,
                                        onValueChange = {
                                            overrideTitle.value = it
                                        },

                                        singleLine = true
                                    )

                                    AnimatedVisibility(
                                        visible = !isSame,
                                        enter = fadeIn() + expandIn(expandFrom = Alignment.CenterEnd),
                                        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                overrideTitle.value = podcast.title
                                                vm.setOverrideTitle(db, podcast, "")
                                            }
                                        ) {
                                            Icon(Icons.Rounded.Restore, "")
                                        }
                                    }
                                }
                            }
                        },

                        index = 0,
                        count = 1,

                        onClick = { }
                    )
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    SettingsHeader(
                        label = stringResource(R.string.view_podcast_settings_subscription)
                    )
                }

                item {
                    SettingsSwitchListItem(
                        checked = subscription.enableAutoDownload,
                        onCheckedChange = {
                            vm.toggleSubscriptionAutoDownload(db, subscription, it)
                        },

                        icon = {
                            Icon(
                                Icons.Rounded.FileDownload,
                                stringResource(R.string.view_podcast_settings_subscription_enable_automatic_downloads)
                            )
                        },
                        label = stringResource(R.string.view_podcast_settings_subscription_enable_automatic_downloads),
                        description = stringResource(R.string.view_podcast_settings_subscription_enable_automatic_downloads_description),

                        index = 0,
                        count = 2
                    )
                }

                item {
                    SettingsSwitchListItem(
                        checked = subscription.enableNotifications,
                        onCheckedChange = {
                            vm.toggleSubscriptionNotifications(db, subscription, it)
                        },

                        icon = {
                            Icon(
                                Icons.Rounded.Notifications,
                                stringResource(R.string.view_podcast_settings_subscription_enable_notifications)
                            )
                        },
                        label = stringResource(R.string.view_podcast_settings_subscription_enable_notifications),
                        description = stringResource(R.string.view_podcast_settings_subscription_enable_notifications_description),

                        index = 1,
                        count = 2
                    )
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }
            }

            item {
                SettingsHeader(
                    label = stringResource(R.string.view_podcast_settings_playback)
                )
            }

            item {
                val seconds = remember { mutableIntStateOf(podcast.skipBeginning) }
                LaunchedEffect(podcast.skipBeginning) { seconds.value = podcast.skipBeginning }

                SettingsSecondsSliderListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.SkipNext,
                            stringResource(R.string.view_podcast_settings_playback_skip_begin_of_show)
                        )
                    },
                    label = stringResource(R.string.view_podcast_settings_playback_skip_begin_of_show),

                    value = seconds.value,
                    onValueChange = { seconds.value = it },

                    max = 180,

                    onValueChangeFinished = {
                        vm.setSkipBeginning(db, podcast, seconds.value)
                    },

                    index = 0,
                    count = 2
                )
            }

            item {
                val seconds = remember { mutableIntStateOf(0) }
                LaunchedEffect(podcast.skipEnding) { seconds.value = podcast.skipEnding }

                SettingsSecondsSliderListItem(
                    icon = {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            stringResource(R.string.view_podcast_settings_playback_skp_end_of_show)
                        )
                    },
                    label = stringResource(R.string.view_podcast_settings_playback_skp_end_of_show),

                    value = seconds.value,
                    onValueChange = { seconds.value = it },

                    max = 180,

                    onValueChangeFinished = {
                        vm.setSkipEnding(db, podcast, seconds.value)
                    },

                    index = 1,
                    count = 2
                )
            }
        }
    }
}