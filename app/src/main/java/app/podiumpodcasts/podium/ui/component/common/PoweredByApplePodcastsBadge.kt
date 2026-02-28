package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PoweredByApplePodcastsBadge() {
    var showBottomSheet by remember { mutableStateOf(false) }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        onClick = {
            showBottomSheet = true
        }
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(12.dp),
                imageVector = Icons.Rounded.Info,
                contentDescription = stringResource(R.string.common_info)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.badge_powered_by_apple_podcasts),
                style = Typography.labelMedium
            )
        }
    }

    if(showBottomSheet) ModalBottomSheet(
        onDismissRequest = {
            showBottomSheet = false
        }
    ) {
        Column(
            Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.badge_powered_by_apple_podcasts),
                style = Typography.headlineMediumEmphasized
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.badge_powered_by_apple_podcasts_text,
                    stringResource(R.string.app_name)
                )
            )
        }
    }
}