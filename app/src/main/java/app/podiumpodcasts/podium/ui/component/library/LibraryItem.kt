package app.podiumpodcasts.podium.ui.component.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.ui.theme.Typography

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    contentDescription: String = "",
    iconContent: @Composable () -> Unit = {
        Icon(
            imageVector = icon!!,
            contentDescription = contentDescription,
            Modifier.size(48.dp)
        )
    },
    badge: @Composable () -> Unit = { },
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier
    ) {
        Column(
            Modifier
                .clip(shape)
                .clickable {
                    onClick()
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                Modifier
                    .widthIn(min = 150.dp, max = 300.dp)
                    .aspectRatio(1f),
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent()
                }
            }

            Text(
                text = title,
                style = Typography.titleLarge,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = -(4).dp)
        ) {
            badge()
        }
    }
}