package app.podiumpodcasts.podium.ui.route.restore

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.layout.LoadingLayout
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.RestoreViewModel
import app.podiumpodcasts.podium.ui.vm.State

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RestoreRoute() {
    val activity = LocalActivity.current

    val context = LocalContext.current
    val db = LocalDatabase.current

    val vm = viewModel<RestoreViewModel>()

    val pickBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { vm.selectFile(context, uri) }
    }

    LaunchedEffect(Unit) { db.close() }

    @Composable
    fun SelectBackupFileButton() {
        val size = ButtonDefaults.MediumContainerHeight
        Button(
            onClick = {
                pickBackupLauncher.launch(
                    arrayOf("application/octet-stream", "application/zip", "*/*")
                )
            },
            modifier = Modifier.heightIn(size),
            contentPadding = ButtonDefaults.contentPaddingFor(size),
        ) {
            Icon(
                imageVector = Icons.Filled.AttachFile,
                contentDescription = stringResource(R.string.route_restore_select_backup_file),
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
            )

            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))

            Text(
                text = stringResource(R.string.route_restore_select_backup_file),
                style = ButtonDefaults.textStyleFor(size)
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    BackButton(
                        icon = Icons.Rounded.Close
                    ) {
                        vm.restartApp(activity)
                    }
                },
                title = {
                    Text(stringResource(R.string.route_restore))
                }
            )
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
                            title = { stringResource(R.string.route_restore_select_backup_file) }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.route_restore_select_backup_file_text,
                                    stringResource(R.string.app_name)
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            SelectBackupFileButton()
                        }
                    }

                    is State.InvalidFile -> {
                        ErrorLayout(
                            title = { stringResource(R.string.route_restore_invalid_backup_file) }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.route_restore_invalid_backup_file_text,
                                    stringResource(R.string.app_name)
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            SelectBackupFileButton()
                        }
                    }

                    is State.OverwriteWarning -> {
                        InfoLayout(
                            icon = Icons.Rounded.Warning,
                            title = { stringResource(R.string.common_are_you_sure) }
                        ) {
                            Text(
                                text = stringResource(R.string.route_restore_confirmation),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            Row {
                                val size = ButtonDefaults.MediumContainerHeight
                                FilledTonalButton(
                                    onClick = {
                                        vm.restartApp(activity)
                                    },
                                    modifier = Modifier.heightIn(size),
                                    contentPadding = ButtonDefaults.contentPaddingFor(size),
                                ) {
                                    Text(
                                        text = stringResource(R.string.common_action_abort),
                                        style = ButtonDefaults.textStyleFor(size)
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        vm.restore(context, state.uri)
                                    },
                                    modifier = Modifier.heightIn(size),
                                    contentPadding = ButtonDefaults.contentPaddingFor(size),
                                ) {
                                    Text(
                                        text = stringResource(R.string.common_action_continue),
                                        style = ButtonDefaults.textStyleFor(size)
                                    )
                                }
                            }
                        }
                    }

                    is State.Unpacking -> {
                        LoadingLayout(
                            size = 96.dp,
                            additionalContent = {
                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.route_restore_unpacking)
                                )
                            }
                        )
                    }

                    is State.Restart -> {
                        InfoLayout(
                            icon = Icons.Rounded.RestartAlt,
                            title = { stringResource(R.string.route_restore_restart_needed) }
                        ) {
                            Text(
                                text = stringResource(R.string.route_restore_restart_needed_text),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            val size = ButtonDefaults.MediumContainerHeight
                            Button(
                                onClick = {
                                    vm.restartApp(activity)
                                },
                                modifier = Modifier.heightIn(size),
                                contentPadding = ButtonDefaults.contentPaddingFor(size),
                            ) {
                                Text(
                                    text = stringResource(R.string.route_restore_action_restart_app),
                                    style = ButtonDefaults.textStyleFor(size)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}