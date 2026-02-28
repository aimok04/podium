package app.podiumpodcasts.podium.ui.component.common.swipeable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import kotlinx.coroutines.launch

@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    startAction: SwipeableItemAction? = null,
    endAction: SwipeableItemAction? = null,
    content: @Composable RowScope.() -> Unit
) {
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState()

    val action = when(dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> startAction
        SwipeToDismissBoxValue.EndToStart -> endAction
        else -> null
    }

    val onAction = action?.onActionHandler()

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromStartToEnd = startAction != null,
        enableDismissFromEndToStart = endAction != null,
        backgroundContent = {
            action ?: return@SwipeToDismissBox
            val style = action.style()

            val backgroundColor by animateColorAsState(
                when(dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> style.backgroundColor
                    else -> style.activeBackgroundColor
                }, label = "swipeBackgroundColor"
            )

            val iconColor by animateColorAsState(
                when(dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> style.iconTint
                    else -> style.activeIconTint
                }, label = "swipeIconColor"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = when(dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    true -> Alignment.CenterStart
                    false -> Alignment.CenterEnd
                }
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = stringResource(R.string.common_action_delete),
                    tint = iconColor,
                    modifier = Modifier.graphicsLayer {
                        scaleX =
                            if(dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 1.0f
                        scaleY =
                            if(dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 1.0f
                    }
                )
            }
        },
        onDismiss = {
            scope.launch {
                if(onAction?.invoke() ?: false)
                    dismissState.reset()
            }
        },
        content = content
    )
}