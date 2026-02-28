package app.podiumpodcasts.podium.ui.component.model.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.ui.formatPubDate
import app.podiumpodcasts.podium.ui.theme.Typography
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastEpisodeListItem(
    modifier: Modifier = Modifier,
    bundle: PodcastEpisodeBundle,
    overlineText: String = formatPubDate(LocalContext.current, bundle.episode.pubDate),
    titleText: String = bundle.episode.title,
    descriptionText: String = bundle.episode.description,
    leadingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = { },
    index: Int = 0,
    count: Int = 0,
    colors: ListItemColors = ListItemDefaults.segmentedColors(),
    onClick: () -> Unit
) {
    SegmentedListItem(
        modifier = if(count == 1)
            modifier.clip(RoundedCornerShape(16.dp))
        else
            modifier,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count
        ),
        colors = colors,
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        overlineContent = {
            Row {
                AnimatedVisibility(
                    visible = bundle.episode.new,
                    enter = fadeIn() + slideInHorizontally { -it },
                    exit = fadeOut() + slideOutHorizontally { -it }
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = stringResource(R.string.common_new_uppercase),
                        style = Typography.labelSmallEmphasized
                    )
                }

                Text(
                    text = overlineText
                )
            }
        }
    ) {
        Column {
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(
                        text = titleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = Typography.titleMediumEmphasized
                    )

                    Text(
                        text = AnnotatedString.fromHtml(
                            htmlString = descriptionText
                                .replace("\n", "<br>")
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        model = bundle.episode.imageUrl,
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(R.string.common_cover_image)
                    )

                    trailingContent()
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PodcastEpisodePlayButton(
                            bundle = bundle
                        )

                        PodcastEpisodeMarkAsPlayedButton(
                            bundle = bundle
                        )
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PodcastEpisodeDownloadButton(
                            icon = true,
                            bundle = bundle
                        )

                        PodcastEpisodeQueueButton(
                            bundle = bundle
                        )
                    }
                }
            }
        }
    }
}