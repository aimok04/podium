package app.podiumpodcasts.podium.ui.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSecondsSliderListItem(
    icon: @Composable () -> Unit,
    label: String,

    value: Int,
    onValueChange: (Int) -> Unit,

    max: Int = 100,

    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,

    trailingContent: @Composable (() -> Unit)? = null,

    index: Int = 0,
    count: Int = 1
) {
    val state = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(value) { state.value = (value / max.toFloat()) }

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
                    value = state.value,
                    onValueChange = {
                        state.value = it
                        onValueChange((max * it).roundToInt())
                    },
                    onValueChangeFinished = onValueChangeFinished
                )
            }
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.template_seconds, value)
            )
        },
        trailingContent = trailingContent,

        onClick = {

        }
    )
}