package app.podiumpodcasts.podium.ui.component.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.theme.Typography

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastCard(
    modifier: Modifier = Modifier,
    podcast: PodcastModel,
    badge: @Composable () -> Unit = { },
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }
                    .padding(8.dp)
                    .width(96.dp)
            ) {
                ShimmerAsyncImage(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp)),

                    model = podcast.imageUrl,
                    contentDescription = null,

                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = podcast.fetchTitle(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.titleSmallEmphasized
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = podcast.author,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography.labelMedium
                )
            }

            Box(
                Modifier.align(Alignment.TopEnd)
            ) {
                badge()
            }
        }
    }
}