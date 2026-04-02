package app.podiumpodcasts.podium.ui.component.model.podcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesFilter
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrder
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodesOrderBy
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.podcast.PodcastFilterBottomSheet
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.podcast.PodcastOrderBottomSheet
import kotlinx.coroutines.delay

class PodcastSearchFilterOrderBarState {

    val searchQuery = mutableStateOf("")

    val filter = mutableStateSetOf<PodcastEpisodesFilter>()
    val negativeFilter = mutableStateSetOf<PodcastEpisodesFilter>()

    val orderBy = mutableStateOf(PodcastEpisodesOrderBy.DATE)
    val order = mutableStateOf(PodcastEpisodesOrder.DESCENDING)

    @Composable
    fun isDefault(): State<Boolean> {
        return remember {
            derivedStateOf {
                searchQuery.value.isEmpty()
                        && filter.isEmpty()
                        && negativeFilter.isEmpty()
                        && orderBy.value == PodcastEpisodesOrderBy.DATE
                        && order.value == PodcastEpisodesOrder.DESCENDING
            }
        }
    }

}

@Composable
fun PodcastSearchFilterOrderBar(
    modifier: Modifier = Modifier,
    state: PodcastSearchFilterOrderBarState
) {
    val showFilterBottomSheet = remember { mutableStateOf(false) }
    val showOrderBottomSheet = remember { mutableStateOf(false) }

    val textValue = remember { mutableStateOf("") }
    LaunchedEffect(textValue.value) {
        delay(300)
        state.searchQuery.value = textValue.value
    }

    fun updateText() {
        state.searchQuery.value = textValue.value
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(CircleShape),

                leadingIcon = {
                    Icon(Icons.Rounded.Search, stringResource(R.string.common_search))
                },
                placeholder = {
                    Text(stringResource(R.string.common_search))
                },

                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),

                value = textValue.value,
                onValueChange = {
                    textValue.value = it
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        updateText()
                    }
                )
            )

            Row {
                FilledIconToggleButton(
                    checked = state.filter.isNotEmpty() || state.negativeFilter.isNotEmpty(),
                    onCheckedChange = {
                        showFilterBottomSheet.value = true
                    }
                ) {
                    Icon(Icons.Rounded.FilterAlt, stringResource(R.string.common_filter))
                }

                FilledIconToggleButton(
                    checked = state.order.value != PodcastEpisodesOrder.DESCENDING || state.orderBy.value != PodcastEpisodesOrderBy.DATE,
                    onCheckedChange = {
                        showOrderBottomSheet.value = true
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Sort, stringResource(R.string.common_order_by))
                }
            }
        }
    }

    if(showFilterBottomSheet.value) PodcastFilterBottomSheet(
        state = state,
        onDismiss = {
            showFilterBottomSheet.value = false
        }
    )

    if(showOrderBottomSheet.value) PodcastOrderBottomSheet(
        state = state,
        onDismiss = {
            showOrderBottomSheet.value = false
        }
    )
}