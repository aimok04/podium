package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialExpressiveTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SwitchableDynamicMaterialExpressiveTheme(
    enable: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit
) {
    when(enable) {
        true -> DynamicMaterialExpressiveTheme(
            seedColor = seedColor,
            content = content
        )

        false -> content()
    }
}