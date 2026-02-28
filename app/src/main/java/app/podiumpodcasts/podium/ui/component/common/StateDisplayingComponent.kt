package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StateDisplayingComponent(
    state: Float,
    minimumState: Float = 0f,
    content: @Composable (left: Boolean) -> Unit
) {
    val displayState = if(state == 0f || state.isNaN())
        0f
    else
        state.coerceAtLeast(minimumState)

    val leftHalf = GenericShape { size, _ ->
        val leftWidth = size.width * displayState
        addRect(Rect(0f, 0f, leftWidth, size.height))
    }

    val rightHalf = GenericShape { size, _ ->
        val leftWidth = size.width * displayState
        addRect(Rect(leftWidth, 0f, size.width, size.height))
    }

    Box {
        Box(
            Modifier.clip(leftHalf)
        ) {
            content(true)
        }

        Box(
            Modifier.clip(rightHalf)
        ) {
            content(false)
        }
    }
}