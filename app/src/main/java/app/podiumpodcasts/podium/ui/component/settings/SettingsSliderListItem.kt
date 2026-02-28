package app.podiumpodcasts.podium.ui.component.settings

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSliderListItem(
    icon: @Composable () -> Unit,
    label: String,

    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,

    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,

    index: Int = 0,
    count: Int = 1
) {
    SettingsListItem(
        icon = icon,
        label = label,
        description = "",

        index = index,
        count = count,

        enabled = enabled,

        content = {
            Column {
                Text(
                    text = label
                )

                Slider(
                    enabled = enabled,
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    steps = steps,
                    onValueChangeFinished = onValueChangeFinished
                )
            }
        },
        supportingContent = supportingContent,
        trailingContent = trailingContent,

        onClick = {

        }
    )
}