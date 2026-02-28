package app.podiumpodcasts.podium.ui.component.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.podiumpodcasts.podium.ui.component.common.StateDisplayingComponent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StateDisplayingIconToggleButton(
    state: Float,
    minimumState: Float = 0f,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    val leftColor = IconButtonDefaults.iconToggleButtonColors(
        containerColor = colors.checkedContainerColor,
        contentColor = colors.checkedContentColor,
        checkedContainerColor = colors.checkedContainerColor,
        checkedContentColor = colors.checkedContentColor
    )

    val rightColor = IconButtonDefaults.iconToggleButtonColors(
        containerColor = colors.containerColor,
        contentColor = colors.contentColor,
        checkedContainerColor = colors.containerColor,
        checkedContentColor = colors.contentColor
    )

    StateDisplayingComponent(
        state = state,
        minimumState = minimumState
    ) { left ->
        IconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = if(left) leftColor else rightColor,
            interactionSource = interactionSource,
            content = content
        )
    }
}