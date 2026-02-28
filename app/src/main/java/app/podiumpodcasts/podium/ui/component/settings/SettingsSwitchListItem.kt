package app.podiumpodcasts.podium.ui.component.settings

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSwitchListItem(
    checked: Boolean,
    onCheckedChange: (newValue: Boolean) -> Unit,

    icon: @Composable () -> Unit,
    label: String,
    description: String,

    enabled: Boolean = true,

    index: Int = 0,
    count: Int = 1
) {
    SettingsListItem(
        icon = icon,
        label = label,
        description = description,

        selected = checked,
        enabled = enabled,

        trailingContent = {
            Switch(
                enabled = enabled,

                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },

        index = index,
        count = count,
        onClick = { onCheckedChange(!checked) }
    )
}