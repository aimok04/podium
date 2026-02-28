package app.podiumpodcasts.podium.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class CountryCodeSelectorState(
    val defaultCountryCode: String
) {
    val show = mutableStateOf(false)
    var value by mutableStateOf(defaultCountryCode)

    fun show() {
        show.value = true
    }

    fun hide() {
        show.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CountryCodeSelectorDialog(
    state: CountryCodeSelectorState,
    onUpdate: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()

    val vm: CountryCodeSelectorViewModel = viewModel()
    val countries by vm.filteredCountries.collectAsState()
    val query by vm.searchQuery.collectAsState()

    if(state.show.value) ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            state.hide()
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),

                leadingIcon = {
                    Icon(Icons.Rounded.Search, stringResource(R.string.common_search))
                },
                placeholder = {
                    Text(stringResource(R.string.common_search))
                },

                value = query,
                onValueChange = {
                    vm.onQueryChange(it)
                }
            )

            HorizontalDivider(
                Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .widthIn(max = 96.dp)
                    .fillMaxWidth()
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    count = countries.size,
                    key = { countries[it].countryCode }
                ) {
                    val country = countries[it]

                    SegmentedListItem(
                        selected = state.value == country.countryCode,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = it,
                            count = countries.size
                        ),

                        leadingContent = {
                            Text(
                                text = country.emojiFlag,
                                fontSize = 24.sp
                            )
                        },
                        overlineContent = {
                            Text(country.countryCode)
                        },
                        content = {
                            Text(country.displayName)
                        },

                        onClick = {
                            scope.launch {
                                state.value = country.countryCode
                                onUpdate()
                            }
                        }
                    )
                }
            }
        }
    }
}

data class Country(
    val countryCode: String,
    val displayName: String,
    val emojiFlag: String
)

internal class CountryCodeSelectorViewModel : ViewModel() {
    private val allCountries = Locale.getISOCountries().map { code ->
        val locale = Locale("", code)

        Country(
            countryCode = code,
            displayName = locale.displayCountry,
            emojiFlag = codeToEmoji(code)
        )
    }.filter { it.displayName.isNotEmpty() }
        .sortedBy { it.displayName }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredCountries = searchQuery.map { query ->
        if(query.isBlank()) allCountries
        else {
            allCountries.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                        it.countryCode.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allCountries)

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun codeToEmoji(code: String): String {
        val first = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }
}