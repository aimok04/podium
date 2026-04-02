package app.podiumpodcasts.podium.ui.component.common.swipeable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SwipeableItem(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    startAction: SwipeableItemAction? = null,
    endAction: SwipeableItemAction? = null,
    content: @Composable RowScope.() -> Unit
) {
    val resetOnNextDismiss = rememberSaveable { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()

    val action = when(dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> startAction
        SwipeToDismissBoxValue.EndToStart -> endAction
        else -> null
    }

    val onAction = action?.onActionHandler()

    val undo = stringResource(R.string.common_action_undo)

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
                    contentDescription = "",
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
                if(resetOnNextDismiss.value) {
                    dismissState.reset()
                    resetOnNextDismiss.value = false
                } else {
                    onAction?.let {
                        resetOnNextDismiss.value = true

                        val result = onAction()
                        if(!result.isDismissed) dismissState.reset()

                        snackbarHostState.currentSnackbarData?.dismiss()

                        val snackBarResult = snackbarHostState.showSnackbar(
                            message = result.message,
                            actionLabel = undo,
                            duration = SnackbarDuration.Short
                        )

                        if(snackBarResult == SnackbarResult.ActionPerformed)
                            result.onUndo()
                    }
                }
            }
        },
        content = content
    )
}