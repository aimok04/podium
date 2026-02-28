package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.podiumpodcasts.podium.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonSearchInputField(
    enabled: Boolean,
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    onSearch: (text: String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
    )

    SearchBarDefaults.InputField(
        enabled = enabled,
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
        onSearch = { onSearch(it) },
        placeholder = {
            Text(
                modifier = Modifier.clearAndSetSemantics {},
                text = stringResource(R.string.common_search)
            )
        },
        leadingIcon = {
            AnimatedContent(
                targetState = searchBarState.currentValue
            ) {
                when(it) {
                    SearchBarValue.Expanded -> {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    searchBarState.animateToCollapsed()
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                stringResource(R.string.common_action_back)
                            )
                        }
                    }

                    else -> {
                        Icon(Icons.Rounded.Search, stringResource(R.string.common_search))
                    }
                }
            }
        }
    )
}