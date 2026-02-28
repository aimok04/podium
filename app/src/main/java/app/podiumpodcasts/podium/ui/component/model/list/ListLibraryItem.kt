package app.podiumpodcasts.podium.ui.component.model.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.api.db.model.ListModel
import app.podiumpodcasts.podium.ui.component.library.LibraryItem
import coil3.compose.AsyncImage

@Composable
fun ListLibraryItem(
    modifier: Modifier = Modifier,
    list: ListModel,
    onClick: () -> Unit
) {
    val systemList = list.systemList()

    LibraryItem(
        modifier = modifier,
        title = systemList?.label?.let { stringResource(it) }
            ?: list.name,
        iconContent = {
            if(systemList != null) {
                Icon(
                    imageVector = systemList.icon,
                    contentDescription = "",
                    Modifier.size(48.dp)
                )
            } else {
                when((list.imageUrls ?: "").isBlank()) {
                    true -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = "",
                            Modifier.size(48.dp)
                        )
                    }

                    false -> {
                        BoxWithConstraints {
                            val size = (maxWidth / 2) - 24.dp

                            FlowRow(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(4) { index ->
                                    Box(
                                        Modifier
                                            .size(size)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        list.getImageUrls().getOrNull(index)?.let { url ->
                                            AsyncImage(
                                                modifier = Modifier.fillMaxSize(),
                                                model = url,
                                                contentScale = ContentScale.Crop,
                                                contentDescription = ""
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        badge = {
            if(list.itemCount > 0) Badge(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(list.itemCount.toString())
            }
        },
        onClick = onClick
    )
}