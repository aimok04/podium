package app.podiumpodcasts.podium.ui.component.common.swipeable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.SystemLists
import app.podiumpodcasts.podium.ui.helper.LocalDatabase

data class SwipeableItemActionStyle(
    val icon: ImageVector,
    val backgroundColor: Color,
    val activeBackgroundColor: Color,
    val iconTint: Color,
    val activeIconTint: Color
)

data class SwipeableItemActionResult(
    val isDismissed: Boolean,
    val message: String,
    val onUndo: suspend () -> Unit
)

abstract class SwipeableItemAction(
    val style: @Composable () -> SwipeableItemActionStyle,
    val onActionHandler: @Composable () -> (suspend () -> SwipeableItemActionResult)
)

interface SwipeableItemActions {
    class DeleteAction(
        onAction: suspend () -> SwipeableItemActionResult
    ) : SwipeableItemAction(
        style = {
            SwipeableItemActionStyle(
                icon = Icons.Rounded.Delete,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                activeBackgroundColor = MaterialTheme.colorScheme.error,
                activeIconTint = MaterialTheme.colorScheme.onError,
            )
        },
        onActionHandler = {
            onAction
        }
    )

    class CheckAction(
        onAction: suspend () -> SwipeableItemActionResult
    ) : SwipeableItemAction(
        style = {
            SwipeableItemActionStyle(
                icon = Icons.Rounded.Check,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                activeBackgroundColor = MaterialTheme.colorScheme.tertiary,
                activeIconTint = MaterialTheme.colorScheme.onTertiary,
            )
        },
        onActionHandler = {
            onAction
        }
    )

    class ResetAction(
        onAction: suspend () -> SwipeableItemActionResult
    ) : SwipeableItemAction(
        style = {
            SwipeableItemActionStyle(
                icon = Icons.Rounded.RestartAlt,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                activeBackgroundColor = MaterialTheme.colorScheme.secondary,
                activeIconTint = MaterialTheme.colorScheme.onSecondary,
            )
        },
        onActionHandler = {
            onAction
        }
    )

    class HearLaterAction(
        episodeId: String
    ) : SwipeableItemAction(
        style = {
            val db = LocalDatabase.current

            val isOnHearLater = remember {
                db.listItems().isOnHearLaterFlow(episodeId)
            }.collectAsState(false)

            SwipeableItemActionStyle(
                icon = when(isOnHearLater.value) {
                    true -> Icons.Rounded.WatchLater
                    false -> Icons.Outlined.WatchLater
                },
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                activeBackgroundColor = MaterialTheme.colorScheme.tertiary,
                activeIconTint = MaterialTheme.colorScheme.onTertiary,
            )
        },
        onActionHandler = actionHandler@{
            val db = LocalDatabase.current

            val removedFromHearLater = stringResource(R.string.snackbar_removed_from_hear_later)
            val addedToHearLater = stringResource(R.string.snackbar_added_to_hear_later)

            return@actionHandler {
                if(db.listItems().isOnHearLater(episodeId)) {
                    val item = db.listItems().get(SystemLists.HEAR_LATER.id, episodeId)
                    db.listItems().deleteAndReindex(
                        listId = SystemLists.HEAR_LATER.id,
                        itemId = item.id,
                        deletedPosition = item.position
                    )

                    SwipeableItemActionResult(
                        isDismissed = false,
                        message = removedFromHearLater,
                        onUndo = {
                            val nextPosition =
                                db.listItems().getNextPosition(SystemLists.HEAR_LATER.id)
                            db.listItems().addListItemAndRefreshItemCount(
                                listId = SystemLists.HEAR_LATER.id,
                                contentId = episodeId,
                                isPodcast = false,
                                position = nextPosition ?: 0
                            )
                        }
                    )
                } else {
                    val nextPosition = db.listItems().getNextPosition(SystemLists.HEAR_LATER.id)
                    db.listItems().addListItemAndRefreshItemCount(
                        listId = SystemLists.HEAR_LATER.id,
                        contentId = episodeId,
                        isPodcast = false,
                        position = nextPosition ?: 0
                    )

                    SwipeableItemActionResult(
                        isDismissed = false,
                        message = addedToHearLater,
                        onUndo = {
                            val item = db.listItems().get(SystemLists.HEAR_LATER.id, episodeId)
                            db.listItems().deleteAndReindex(
                                listId = SystemLists.HEAR_LATER.id,
                                itemId = item.id,
                                deletedPosition = item.position
                            )
                        }
                    )
                }
            }
        }
    )
}