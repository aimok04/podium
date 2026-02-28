package app.podiumpodcasts.podium.ui.component.model

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.theme.Typography
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastListItem(
    modifier: Modifier = Modifier,
    podcast: PodcastModel,
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

        leadingContent = {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp)),
                model = podcast.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.common_cover_image)
            )
        },
        supportingContent = {
            Text(
                text = AnnotatedString.fromHtml(
                    htmlString = podcast.description
                        .replace("\n", "<br>")
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    ) {
        Text(
            text = podcast.fetchTitle(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = Typography.titleMediumEmphasized
        )
    }
}