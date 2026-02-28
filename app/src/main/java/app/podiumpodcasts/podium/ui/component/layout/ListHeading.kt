package app.podiumpodcasts.podium.ui.component.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.ui.theme.Typography

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListHeading(
    heading: String,
    contentPadding: PaddingValues = PaddingValues(12.dp)
) {
    Box(
        Modifier.padding(contentPadding)
    ) {
        Text(
            text = heading,
            color = MaterialTheme.colorScheme.primary,
            style = Typography.titleMediumEmphasized
        )
    }
}