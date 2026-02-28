package app.podiumpodcasts.podium.ui.component.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.ButtonLabelWithIconInset
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.SaveToListBottomSheet
import app.podiumpodcasts.podium.ui.helper.LocalDatabase

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContentSaveToListButton(
    contentId: String,
    isPodcast: Boolean
) {
    val db = LocalDatabase.current

    val showBottomSheet = remember { mutableStateOf(false) }

    val isSaved = db.listItems().exists(contentId).collectAsState(false)

    TonalToggleButton(
        checked = isSaved.value,
        onCheckedChange = {
            showBottomSheet.value = true
        }
    ) {
        AnimatedContent(
            targetState = isSaved.value
        ) { isSaved ->
            ButtonLabelWithIconInset(
                icon = when(isSaved) {
                    true -> Icons.Rounded.Bookmarks
                    false -> Icons.Rounded.BookmarkBorder
                },
                label = when(isSaved) {
                    true -> stringResource(R.string.common_saved)
                    false -> stringResource(R.string.common_action_save)
                }
            )
        }
    }

    if(showBottomSheet.value) SaveToListBottomSheet(
        contentId = contentId,
        isPodcast = isPodcast
    ) {
        showBottomSheet.value = false
    }
}