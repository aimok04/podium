package app.podiumpodcasts.podium.ui.view.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.component.DetailsList
import app.podiumpodcasts.podium.ui.component.DetailsListItemModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.ContentFavoriteButton
import app.podiumpodcasts.podium.ui.component.model.ContentSaveToListButton
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeDownloadButton
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodeMarkAsPlayedButton
import app.podiumpodcasts.podium.ui.component.model.episode.PodcastEpisodePlayButton
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.formatEpisodePlayTime
import app.podiumpodcasts.podium.ui.formatPubDate
import app.podiumpodcasts.podium.ui.theme.Typography
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PodcastEpisodeDetailView(
    bundle: PodcastEpisodeBundle,
    parent: PodcastModel?,
    showParentLink: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onShowParent: () -> Unit = { },
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val episode = bundle.episode

    val BACKDROP_SIZE = 200.dp

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton {
                        onBack()
                    }
                },
                actions = {
                    if(showParentLink) FilledIconButton(
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        onClick = {
                            onShowParent()
                        }
                    ) {
                        ShimmerAsyncImage(
                            modifier = Modifier
                                .size(IconButtonDefaults.mediumIconSize)
                                .clip(CircleShape),

                            model = parent?.imageUrl,
                            contentDescription = stringResource(R.string.common_cover_image)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                title = { }
            )
        },
        containerColor = backgroundColor
    ) { insets ->
        Box {
            ShimmerAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(insets.calculateTopPadding() + BACKDROP_SIZE)
                    .hazeEffect(),

                model = episode.imageUrl ?: parent?.imageUrl,
                contentDescription = null,

                contentScale = ContentScale.Crop
            )

            Box(
                Modifier.verticalScroll(
                    state = rememberScrollState()
                )
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(insets.calculateTopPadding() + BACKDROP_SIZE)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    backgroundColor
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

                        model = episode.imageUrl ?: parent?.imageUrl,
                        contentDescription = null,

                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    Modifier.padding(insets)
                ) {
                    Spacer(Modifier.height(BACKDROP_SIZE))

                    Column(
                        Modifier.background(backgroundColor)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        SelectionContainer(
                            Modifier
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            Column {
                                Text(
                                    text = episode.title,
                                    style = Typography.headlineLargeEmphasized
                                )

                                Spacer(Modifier.height(4.dp))

                                Row {
                                    AnimatedVisibility(
                                        visible = bundle.episode.new,
                                        enter = fadeIn() + expandIn(
                                            expandFrom = Alignment.CenterStart
                                        ),
                                        exit = fadeOut() + shrinkOut(
                                            shrinkTowards = Alignment.CenterStart
                                        )
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(end = 16.dp),
                                            text = stringResource(R.string.common_new_uppercase),
                                            style = Typography.labelLargeEmphasized
                                        )
                                    }

                                    Text(
                                        text = formatPubDate(LocalContext.current, episode.pubDate),
                                        style = Typography.labelLarge
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(16.dp))

                            PodcastEpisodePlayButton(
                                bundle = bundle,
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            PodcastEpisodeMarkAsPlayedButton(
                                bundle = bundle,

                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )

                            Spacer(Modifier.width(8.dp))

                            VerticalDivider(
                                Modifier.height(24.dp)
                            )

                            Spacer(Modifier.width(16.dp))

                            PodcastEpisodeDownloadButton(
                                icon = false,
                                bundle = bundle
                            )

                            Spacer(Modifier.width(8.dp))

                            ContentSaveToListButton(
                                contentId = episode.id,
                                isPodcast = false
                            )

                            Spacer(Modifier.width(4.dp))

                            ContentFavoriteButton(
                                contentId = episode.id,
                                isPodcast = false
                            )

                            Spacer(Modifier.width(8.dp))
                        }

                        Column(
                            Modifier.padding(16.dp)
                        ) {
                            var canExpandDescription by remember { mutableStateOf(false) }
                            var expandDescription by remember { mutableStateOf(false) }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(canExpandDescription || expandDescription) {
                                        expandDescription = !expandDescription
                                    },
                                color = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                SelectionContainer(
                                    Modifier.padding(16.dp)
                                ) {
                                    AnimatedContent(expandDescription) {
                                        when(it) {
                                            true -> Text(
                                                text = AnnotatedString.fromHtml(
                                                    htmlString = episode.description
                                                        .replace("\n", "<br>")
                                                )
                                            )

                                            false -> Text(
                                                text = AnnotatedString.fromHtml(
                                                    htmlString = episode.description
                                                        .replace("\n", "<br>")
                                                ),
                                                maxLines = 5,
                                                overflow = TextOverflow.Ellipsis,
                                                onTextLayout = { result ->
                                                    canExpandDescription = result.hasVisualOverflow
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            DetailsList(
                                items = listOf(
                                    DetailsListItemModel(
                                        icon = Icons.Rounded.Person,
                                        label = R.string.common_author,
                                        value = episode.author
                                    ),
                                    DetailsListItemModel(
                                        icon = Icons.Rounded.AccessTime,
                                        label = R.string.common_duration,
                                        value = formatEpisodePlayTime(
                                            context = LocalContext.current,
                                            duration = episode.duration
                                        )
                                    ),
                                    DetailsListItemModel(
                                        icon = Icons.Rounded.Link,
                                        label = R.string.common_source,
                                        value = episode.link,
                                        onClick = {
                                            uriHandler.openUri(episode.link)
                                        }
                                    ),
                                    DetailsListItemModel(
                                        icon = Icons.Rounded.RssFeed,
                                        label = R.string.common_rss_feed,
                                        value = episode.origin
                                    ),
                                    DetailsListItemModel(
                                        icon = Icons.Rounded.AudioFile,
                                        label = R.string.common_audio_file,
                                        value = episode.audioUrl,
                                        onClick = {
                                            uriHandler.openUri(episode.audioUrl)
                                        }
                                    ),
                                    DetailsListItemModel(
                                        icon = Icons.AutoMirrored.Rounded.Label,
                                        label = R.string.common_guid,
                                        value = episode.guid
                                    )
                                )
                            )
                        }
                    }

                    FloatingMediaPlayerSpacer()
                }
            }
        }
    }
}