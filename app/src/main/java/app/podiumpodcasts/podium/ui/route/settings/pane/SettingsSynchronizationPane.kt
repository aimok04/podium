package app.podiumpodcasts.podium.ui.route.settings.pane

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.background.worker.sync.FullSynchronizationWorker
import app.podiumpodcasts.podium.background.worker.sync.PartialSynchronizationWorker
import app.podiumpodcasts.podium.manager.SyncManager
import app.podiumpodcasts.podium.ui.component.common.ExperimentalBadge
import app.podiumpodcasts.podium.ui.component.layout.InfoLayout
import app.podiumpodcasts.podium.ui.component.settings.SettingsListItem
import app.podiumpodcasts.podium.ui.component.settings.SettingsSwitchListItem
import app.podiumpodcasts.podium.ui.formatPubDate
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.route.settings.SettingsPaneKey
import app.podiumpodcasts.podium.ui.vm.GpodderLoginState
import app.podiumpodcasts.podium.ui.vm.SettingsSynchronizationViewModel
import app.podiumpodcasts.podium.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@SuppressLint("ParcelCreator")
@Serializable
class SettingsSynchronizationKey : SettingsPaneKey()

@UnstableApi
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsSynchronizationPane(
    navigationIcon: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val db = LocalDatabase.current
    val settingsRepository = LocalSettingsRepository.current

    val vm = viewModel { SettingsViewModel(db, settingsRepository) }
    val syncVm = viewModel { SettingsSynchronizationViewModel(db, settingsRepository) }

    val timestampSubscriptions = vm.repository.sync.timestampSubscriptions.collectAsState(0L)

    val enable = vm.repository.sync.enable.collectAsState(false)
    val type = vm.repository.sync.type.collectAsState("gpodder")

    val deviceId = vm.repository.sync.deviceId.collectAsState("-")

    val mBaseUrl = vm.repository.sync.baseUrl.collectAsState("https://gpodder.net")
    val baseUrl = remember { mutableStateOf("https://gpodder.net") }
    LaunchedEffect(mBaseUrl.value) { baseUrl.value = mBaseUrl.value }

    val auth = vm.repository.sync.auth.collectAsState("")

    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = navigationIcon,

                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.route_settings_synchronization),
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedVisibility(
                            visible = timestampSubscriptions.value != 0L
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.route_settings_synchronization_last_synced,
                                    remember {
                                        formatPubDate(
                                            context,
                                            timestampSubscriptions.value
                                        )
                                    }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),

                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                item {
                    SettingsSwitchListItem(
                        checked = enable.value,
                        onCheckedChange = { enable ->
                            scope.launch {
                                vm.repository.sync.setEnable(enable)

                                if(deviceId.value.isEmpty()) {
                                    val deviceCaption = SyncManager.generateDeviceCaption(context)
                                    val deviceId = SyncManager.generateDeviceId(deviceCaption)
                                    vm.repository.sync.setDeviceCaption(deviceCaption)
                                    vm.repository.sync.setDeviceId(deviceId)
                                }
                            }
                        },

                        label = stringResource(R.string.route_settings_synchronization_enable),

                        index = 0,
                        count = 1
                    )
                }

                if(enable.value) {

                    item {
                        Spacer(
                            Modifier
                                .height(32.dp)
                                .animateItem()
                        )
                    }

                    item {
                        SettingsListItem(
                            modifier = Modifier.animateItem(),

                            label = stringResource(R.string.route_settings_synchronization_synchronize_now),

                            index = 0,
                            count = 2,

                            onClick = {
                                scope.launch {
                                    PartialSynchronizationWorker.enqueue(context)
                                }
                            }
                        )
                    }

                    item {
                        SettingsListItem(
                            modifier = Modifier.animateItem(),

                            label = stringResource(R.string.route_settings_synchronization_full_synchronize),

                            index = 1,
                            count = 2,

                            onClick = {
                                scope.launch {
                                    FullSynchronizationWorker.enqueue(context)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(
                            Modifier
                                .height(32.dp)
                                .animateItem()
                        )
                    }

                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),

                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Language,
                                    stringResource(R.string.route_settings_synchronization_base_url)
                                )
                            },
                            label = {
                                Text(stringResource(R.string.route_settings_synchronization_base_url))
                            },

                            value = baseUrl.value,
                            onValueChange = { value ->
                                baseUrl.value = value
                            },

                            singleLine = true,

                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Uri
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    scope.launch {
                                        vm.repository.sync.setBaseUrl(baseUrl.value)
                                        syncVm.resetAuth()
                                    }
                                }
                            )
                        )
                    }

                    item {
                        Spacer(
                            Modifier
                                .height(32.dp)
                                .animateItem()
                        )
                    }

                    item {
                        PrimaryTabRow(
                            modifier = Modifier.animateItem(),
                            selectedTabIndex = when(type.value) {
                                "gpodder" -> 0
                                "nextcloud" -> 1
                                else -> 0
                            }
                        ) {
                            Tab(
                                selected = type.value == "gpodder",
                                onClick = {
                                    syncVm.setType("gpodder")
                                },
                                text = {
                                    Text(
                                        text = "gpodder.net",
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )

                            Tab(
                                selected = type.value == "nextcloud",
                                onClick = {
                                    syncVm.setType("nextcloud")
                                },
                                text = {
                                    Text(
                                        text = "Nextcloud",
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(
                            Modifier
                                .height(16.dp)
                                .animateItem()
                        )
                    }

                    if(type.value == "gpodder") {
                        if(auth.value.isNotEmpty()) {
                            item {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {

                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = stringResource(R.string.common_action_logout),
                                        modifier = Modifier.size(
                                            ButtonDefaults.iconSizeFor(
                                                ButtonDefaults.MinHeight
                                            )
                                        ),
                                    )

                                    Spacer(
                                        Modifier.size(
                                            ButtonDefaults.iconSpacingFor(
                                                ButtonDefaults.MinHeight
                                            )
                                        )
                                    )

                                    Text(stringResource(R.string.common_action_logout))
                                }
                            }
                        } else {
                            item {
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),

                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.AccountCircle,
                                            stringResource(R.string.common_username)
                                        )
                                    },
                                    label = {
                                        Text(stringResource(R.string.common_username))
                                    },

                                    singleLine = true,

                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email
                                    ),

                                    value = username.value,
                                    onValueChange = { value ->
                                        username.value = value
                                    }
                                )
                            }

                            item {
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),

                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Password,
                                            stringResource(R.string.common_password)
                                        )
                                    },
                                    label = {
                                        Text(stringResource(R.string.common_password))
                                    },

                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),

                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password
                                    ),

                                    value = password.value,
                                    onValueChange = { value ->
                                        password.value = value
                                    }
                                )
                            }

                            item {
                                Spacer(Modifier.height(16.dp))
                            }

                            item {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {
                                        syncVm.gpodderLogin(context, username.value, password.value)
                                    }
                                ) {
                                    AnimatedContent(
                                        targetState = syncVm.gpodderLoginState.value
                                    ) { state ->
                                        when(state) {
                                            GpodderLoginState.Loading -> {
                                                ContainedLoadingIndicator()
                                            }

                                            else -> {
                                                Row {
                                                    Icon(
                                                        Icons.AutoMirrored.Rounded.Login,
                                                        contentDescription = stringResource(R.string.common_action_login),
                                                        modifier = Modifier.size(
                                                            ButtonDefaults.iconSizeFor(
                                                                ButtonDefaults.MinHeight
                                                            )
                                                        ),
                                                    )

                                                    Spacer(
                                                        Modifier.size(
                                                            ButtonDefaults.iconSpacingFor(
                                                                ButtonDefaults.MinHeight
                                                            )
                                                        )
                                                    )

                                                    Text(stringResource(R.string.common_action_login))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if(syncVm.gpodderLoginState.value is GpodderLoginState.Failure) {
                                item {
                                    Spacer(Modifier.height(8.dp))
                                }

                                item {
                                    val message =
                                        (syncVm.gpodderLoginState.value as? GpodderLoginState.Failure)?.message
                                            ?: stringResource(R.string.common_invalid_credentials)

                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else if(type.value == "nextcloud") {
                        item {
                            InfoLayout(
                                modifier = Modifier.animateItem(),
                                icon = Icons.Rounded.Warning,
                                title = { "Not supported yet" },
                                content = { Text("Support for nextcloud-gpodder will be added in a future version.") }
                            )
                        }
                    }
                }
            }


            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                ExperimentalBadge()
            }
        }
    }
}