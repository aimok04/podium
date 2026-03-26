package app.podiumpodcasts.podium.ui.route.importing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.manager.PodcastManager
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.layout.LoadingLayout
import app.podiumpodcasts.podium.ui.component.layout.StateIconLayout
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.importing.OpmlImportingViewModel
import app.podiumpodcasts.podium.ui.vm.importing.State

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OpmlImportingRoute(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = LocalDatabase.current

    val vm = remember { OpmlImportingViewModel() }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { vm.selectFile(context, db, uri) }
    }

    @Composable
    fun SelectOpmlFileButton() {
        val size = ButtonDefaults.MediumContainerHeight
        Button(
            onClick = {
                pickFileLauncher.launch(
                    arrayOf("*/*")
                )
            },
            modifier = Modifier.heightIn(size),
            contentPadding = ButtonDefaults.contentPaddingFor(size),
        ) {
            Icon(
                imageVector = Icons.Filled.AttachFile,
                contentDescription = stringResource(R.string.common_action_select_file),
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
            )

            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))

            Text(
                text = stringResource(R.string.common_action_select_file),
                style = ButtonDefaults.textStyleFor(size)
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    BackButton {
                        onBack()
                    }
                },
                title = {
                    Text(stringResource(R.string.route_import_ompl))
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = vm.state.value is State.SelectOutlines && vm.selectedOrigins.isNotEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BottomAppBar(
                    actions = { },
                    floatingActionButton = {
                        FloatingActionButton(
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                            onClick = {
                                vm.add(context, PodcastManager(db))
                            }
                        ) {
                            Icon(Icons.Rounded.Add, stringResource(R.string.common_action_add))
                        }
                    }
                )
            }
        }
    ) { inset ->
        Box(
            Modifier
                .padding(inset)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = vm.state.value
            ) { state ->
                when(state) {
                    is State.SelectFile -> {
                        InfoLayout(
                            icon = Icons.Rounded.UploadFile,
                            title = { stringResource(R.string.route_import_select_backup_file) }
                        ) {
                            Text(
                                text = stringResource(R.string.route_import_select_backup_file_text_opml),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            SelectOpmlFileButton()
                        }
                    }

                    is State.InvalidFile -> {
                        ErrorLayout(
                            title = { stringResource(R.string.route_import_invalid_file) }
                        ) {
                            Text(
                                text = stringResource(R.string.route_import_invalid_file_text_opml),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            SelectOpmlFileButton()

                            Spacer(Modifier.height(48.dp))

                            LazyColumn(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item {
                                    Text(
                                        modifier = Modifier.alpha(0.4f),
                                        text = state.reason,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        lineHeight = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    is State.SelectOutlines -> {
                        val count = state.file.body.outlines.size

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                        ) {
                            item {
                                Column {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = state.file.head.title,
                                        maxLines = 1,
                                        style = Typography.headlineMediumEmphasized,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )

                                    state.file.head.dateCreated?.let {
                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = state.file.head.dateCreated,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))
                                }
                            }

                            items(
                                count = state.file.body.outlines.size
                            ) { index ->
                                val outline = state.file.body.outlines[index]

                                val checked = vm.selectedOrigins.contains(outline.xmlUrl)
                                val alreadyImported = vm.existingOrigins.contains(outline.xmlUrl)

                                fun toggle() {
                                    if(checked) {
                                        vm.selectedOrigins.remove(outline.xmlUrl)
                                    } else {
                                        vm.selectedOrigins.add(outline.xmlUrl!!)
                                    }
                                }

                                SegmentedListItem(
                                    enabled = !alreadyImported,

                                    selected = checked,
                                    shapes = ListItemDefaults.segmentedShapes(
                                        index = index,
                                        count = count
                                    ),

                                    overlineContent = if(alreadyImported) {
                                        {
                                            Text(
                                                text = stringResource(R.string.route_import_podcast_already_imported),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    content = {
                                        Text(outline.text)
                                    },
                                    supportingContent = {
                                        Text(outline.xmlUrl ?: "")
                                    },

                                    trailingContent = {
                                        Checkbox(
                                            enabled = !alreadyImported,
                                            checked = checked,
                                            onCheckedChange = {
                                                toggle()
                                            }
                                        )
                                    },

                                    onClick = {
                                        toggle()
                                    }
                                )
                            }
                        }
                    }

                    is State.Unpacking -> {
                        LoadingLayout(
                            size = 96.dp,
                            additionalContent = {
                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.route_import_unpacking)
                                )
                            }
                        )
                    }

                    is State.Done -> {
                        StateIconLayout(
                            icon = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.common_done)
                        ) {
                            onBack()
                        }
                    }
                }
            }
        }
    }
}