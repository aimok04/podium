package app.podiumpodcasts.podium.ui.component.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.SystemLists
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContentFavoriteButton(
    contentId: String,
    isPodcast: Boolean,
    colors: IconToggleButtonColors = IconButtonDefaults.filledTonalIconToggleButtonColors()
) {
    val scope = rememberCoroutineScope()
    val db = LocalDatabase.current

    val isFavorite = db.listItems().isFavorite(contentId).collectAsState(false)

    FilledIconToggleButton(
        checked = isFavorite.value,
        onCheckedChange = {
            scope.launch {
                if(isFavorite.value) {
                    val item = db.listItems().get(
                        listId = SystemLists.FAVORITES.id,
                        contentId = contentId
                    )

                    db.listItems().deleteAndReindex(
                        listId = item.listId,
                        itemId = item.id,
                        deletedPosition = item.position
                    )
                } else {
                    val position = db.listItems().getNextPosition(SystemLists.FAVORITES.id)
                        ?: 0

                    db.listItems().addListItemAndRefreshItemCount(
                        listId = SystemLists.FAVORITES.id,
                        contentId = contentId,
                        isPodcast = isPodcast,
                        position = position
                    )
                }
            }
        },

        colors = colors
    ) {
        AnimatedContent(
            targetState = isFavorite.value
        ) { isFavorite ->
            when(isFavorite) {
                true -> {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = stringResource(R.string.common_action_remove_from_favorites)
                    )
                }

                false -> {
                    Icon(
                        imageVector = Icons.Rounded.StarBorder,
                        contentDescription = stringResource(R.string.common_action_add_to_favorites)
                    )
                }
            }
        }
    }
}