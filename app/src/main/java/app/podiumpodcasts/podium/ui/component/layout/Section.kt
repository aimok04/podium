package app.podiumpodcasts.podium.ui.component.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.theme.Typography

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Section(
    modifier: Modifier,
    title: String,
    badge: (@Composable () -> Unit)? = null,
    onClickExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier.padding(top = 16.dp, bottom = 16.dp)
    ) {
        Box(
            Modifier
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClickExpand() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(badge != null) {
                    badge()

                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = Typography.titleMediumEmphasized
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Rounded.DoubleArrow,
                    contentDescription = stringResource(R.string.common_action_expand),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        content()
    }
}