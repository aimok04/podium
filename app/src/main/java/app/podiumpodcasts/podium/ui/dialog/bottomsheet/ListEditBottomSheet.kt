package app.podiumpodcasts.podium.ui.dialog.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.list.ListEditBottomSheetUIState
import app.podiumpodcasts.podium.ui.vm.list.ListEditBottomSheetViewModel

class ListEditBottomSheetState {

    val listId = mutableIntStateOf(0)
    val shown = mutableStateOf(false)

    fun show(id: Int) {
        listId.value = id
        shown.value = true
    }

    fun hide() {
        shown.value = false
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListEditBottomSheet(
    state: ListEditBottomSheetState
) {
    if(!state.shown.value) return

    val db = LocalDatabase.current
    val vm = remember { ListEditBottomSheetViewModel() }

    val list = db.lists().get(state.listId.value)
        .collectAsState(null)

    LaunchedEffect(list.value) {
        vm.name.value = list.value?.name ?: ""
        vm.description.value = list.value?.description ?: ""
    }

    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(vm.done.value) {
        if(!vm.done.value) return@LaunchedEffect
        sheetState.hide()
        state.hide()
    }

    ModalBottomSheet(
        onDismissRequest = { state.hide() },
        sheetState = sheetState
    ) {
        Box(
            Modifier.padding(24.dp)
        ) {
            AnimatedContent(
                targetState = vm.state.value
            ) { _state ->
                when(_state) {
                    is ListEditBottomSheetUIState.Idle -> Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.dialog_edit_list_title),
                            style = Typography.headlineMediumEmphasized,
                            textAlign = TextAlign.Center
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth(),

                            value = vm.name.value,
                            onValueChange = {
                                vm.name.value = it
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    contentDescription = stringResource(R.string.form_name)
                                )
                            },

                            label = {
                                Text(stringResource(R.string.form_name))
                            },

                            singleLine = true,
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth(),

                            value = vm.description.value,
                            onValueChange = {
                                vm.description.value = it
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Description,
                                    contentDescription = stringResource(R.string.form_description)
                                )
                            },

                            label = {
                                Text(stringResource(R.string.form_description))
                            }
                        )

                        Button(
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                vm.edit(db, state.listId.value)
                            }
                        ) {
                            Text(stringResource(R.string.common_action_edit))
                        }
                    }

                    is ListEditBottomSheetUIState.Error -> {
                        ErrorLayout {
                            Text(_state.reason)
                        }
                    }
                }
            }
        }
    }
}