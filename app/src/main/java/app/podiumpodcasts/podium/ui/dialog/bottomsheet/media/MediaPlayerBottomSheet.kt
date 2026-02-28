package app.podiumpodcasts.podium.ui.dialog.bottomsheet.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.IconButtonWidthOption
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.SwitchableDynamicMaterialExpressiveTheme
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.formatPlayerTime
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.MediaPlayerViewModel
import app.podiumpodcasts.podium.ui.vm.SourceTypeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPlayerBottomSheet(
    onOpenEpisode: (
        origin: String,
        id: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val compactLayout = !windowSizeClass.isHeightAtLeastBreakpoint(500)

    val settingsRepository = LocalSettingsRepository.current
    val vm = viewModel<MediaPlayerViewModel>()

    val enableArtworkColors = settingsRepository.appearance.enableArtworkColors.collectAsState(true)

    var blockSliderUpdate by remember { mutableStateOf(false) }

    var sliderState by remember { mutableFloatStateOf(0f) }

    val animateSliderState = remember { Animatable(0f) }
    LaunchedEffect(animateSliderState.value) {
        if(blockSliderUpdate) return@LaunchedEffect
        sliderState = animateSliderState.value
    }

    val progressState = vm.getProgressState()
    LaunchedEffect(progressState.value) {
        if(blockSliderUpdate) return@LaunchedEffect
        animateSliderState.snapTo(sliderState)
        animateSliderState.animateTo(progressState.value, tween(100))
    }

    LaunchedEffect(vm.isPlayerVisible) {
        if(vm.isPlayerVisible) return@LaunchedEffect
        onDismiss()
    }

    val sleepTimerState = vm.getSleepTimerState()

    var showSleepTimerBottomSheet by remember { mutableStateOf(false) }
    var showSpeedBottomSheet by remember { mutableStateOf(false) }
    var showQueueBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    fun dismissGracefully(
        after: () -> Unit
    ) = scope.launch {
        sheetState.hide()

        after()
        onDismiss()
    }

    SwitchableDynamicMaterialExpressiveTheme(
        enable = enableArtworkColors.value,
        seedColor = Color(vm.metadataImageSeedColor ?: 1)
    ) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = vm.mediaSourceType
                    ) {
                        when(it) {
                            is SourceTypeState.Downloaded -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Row(
                                        Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(12.dp),
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = stringResource(R.string.common_downloaded)
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Text(
                                            text = stringResource(R.string.common_downloaded),
                                            style = Typography.labelMedium
                                        )
                                    }
                                }
                            }

                            is SourceTypeState.Streaming -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Row(
                                        Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(12.dp),
                                            imageVector = Icons.Rounded.Podcasts,
                                            contentDescription = stringResource(R.string.common_info)
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Text(
                                            text = stringResource(R.string.common_streaming),
                                            style = Typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                dismissGracefully {
                                    onOpenEpisode(
                                        vm.metadataOrigin!!,
                                        vm.metadataEpisodeId!!
                                    )
                                }
                            }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if(!compactLayout) {
                            Image(
                                mediaPlayerViewModel = vm,
                                compactLayout = false
                            )

                            Spacer(Modifier.height(32.dp))
                        }

                        AnimatedContent(vm.mediaMetadata) { mediaMetadata ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if(compactLayout) {
                                    Image(
                                        mediaPlayerViewModel = vm,
                                        compactLayout = true
                                    )

                                    Spacer(Modifier.width(32.dp))
                                }

                                Column(
                                    Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = mediaMetadata?.title.toString(),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = Typography.displaySmallEmphasized
                                    )

                                    Text(
                                        text = mediaMetadata?.subtitle.toString(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = Typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider(
                            modifier = Modifier.fillMaxWidth(),
                            value = sliderState,
                            onValueChange = {
                                sliderState = it
                                blockSliderUpdate = true
                            },
                            onValueChangeFinished = {
                                blockSliderUpdate = true
                                vm.seek((sliderState * vm.currentDuration).toLong())

                                // don't animate slider after seeking
                                scope.launch {
                                    delay(500)
                                    blockSliderUpdate = false
                                }
                            }
                        )

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatPlayerTime(
                                    time = (sliderState * vm.currentDuration).toLong(),
                                    duration = vm.currentDuration
                                ),
                                style = Typography.labelMediumEmphasized
                            )

                            Text(
                                text = formatPlayerTime(
                                    time = vm.currentDuration
                                ),
                                style = Typography.labelMediumEmphasized
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        ButtonGroup(
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalIconButton(
                                    modifier = Modifier
                                        .size(IconButtonDefaults.largeContainerSize()),
                                    shapes = IconButtonDefaults.shapes(
                                        shape = IconButtonDefaults.largeSquareShape,
                                        pressedShape = IconButtonDefaults.largePressedShape
                                    ),
                                    onClick = {
                                        vm.replay10()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Replay10,
                                        contentDescription = stringResource(R.string.common_action_seek_back),
                                        modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                                    )
                                }

                                val targetSizeWidth by animateDpAsState(
                                    targetValue = if(vm.isPlaying) {
                                        IconButtonDefaults.largeContainerSize(IconButtonWidthOption.Uniform).width
                                    } else {
                                        IconButtonDefaults.largeContainerSize(IconButtonWidthOption.Wide).width
                                    },
                                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                )

                                FilledIconButton(
                                    modifier = Modifier
                                        .height(
                                            IconButtonDefaults
                                                .largeContainerSize(IconButtonWidthOption.Narrow)
                                                .height
                                        )
                                        .width(targetSizeWidth),
                                    shapes = IconButtonDefaults.shapes(
                                        shape = IconButtonDefaults.largeSquareShape,
                                        pressedShape = IconButtonDefaults.largePressedShape
                                    ),
                                    onClick = {
                                        if(vm.isPlaying) {
                                            vm.pause()
                                        } else {
                                            vm.play()
                                        }
                                    }
                                ) {
                                    AnimatedContent(vm.isPlaying) {
                                        Icon(
                                            imageVector = when(it) {
                                                true -> Icons.Rounded.Pause
                                                false -> Icons.Rounded.PlayArrow
                                            },
                                            contentDescription = when(it) {
                                                true -> stringResource(R.string.common_action_pause)
                                                false -> stringResource(R.string.common_action_play)
                                            },
                                            modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                                        )
                                    }
                                }

                                FilledTonalIconButton(
                                    modifier = Modifier
                                        .size(IconButtonDefaults.largeContainerSize()),
                                    shapes = IconButtonDefaults.shapes(
                                        shape = IconButtonDefaults.largeSquareShape,
                                        pressedShape = IconButtonDefaults.largePressedShape
                                    ),
                                    onClick = {
                                        vm.forward10()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Forward10,
                                        contentDescription = stringResource(R.string.common_action_seek_forwards),
                                        modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                                    )
                                }
                            }
                        }
                    }
                }

                FlowRow(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val size = ButtonDefaults.ExtraSmallContainerHeight
                    val colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )

                    AnimatedContent(sleepTimerState.value) { sleepTimerState ->
                        Button(
                            modifier = Modifier
                                .heightIn(size)
                                .padding(4.dp),
                            contentPadding = ButtonDefaults.contentPaddingFor(size),
                            shapes = ButtonDefaults.shapesFor(size),
                            colors = when(sleepTimerState) {
                                null -> colors
                                else -> ButtonDefaults.buttonColors()
                            },
                            onClick = { showSleepTimerBottomSheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Bedtime,
                                    contentDescription = stringResource(R.string.common_sleep_timer),
                                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                                )

                                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))

                                Text(
                                    text = when(sleepTimerState) {
                                        null -> stringResource(R.string.common_sleep_timer)
                                        else -> stringResource(
                                            R.string.template_min,
                                            sleepTimerState
                                        )
                                    },
                                    style = ButtonDefaults.textStyleFor(size)
                                )
                            }
                        }
                    }

                    AnimatedContent(vm.speed) { speed ->
                        Button(
                            modifier = Modifier
                                .heightIn(size)
                                .padding(4.dp),
                            contentPadding = ButtonDefaults.contentPaddingFor(size),
                            shapes = ButtonDefaults.shapesFor(size),
                            colors = when(speed) {
                                1f -> colors
                                else -> ButtonDefaults.buttonColors()
                            },
                            onClick = { showSpeedBottomSheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Speed,
                                    contentDescription = stringResource(R.string.common_playback_speed),
                                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                                )

                                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))

                                Text(
                                    text = when(speed) {
                                        1f -> stringResource(R.string.common_playback_speed)
                                        else -> String.format(
                                            Locale.getDefault(),
                                            "%.2f",
                                            speed
                                        ) + "x"
                                    },
                                    style = ButtonDefaults.textStyleFor(size)
                                )
                            }
                        }
                    }

                    if(vm.queue.isNotEmpty()) Button(
                        modifier = Modifier
                            .heightIn(size)
                            .padding(4.dp),
                        contentPadding = ButtonDefaults.contentPaddingFor(size),
                        shapes = ButtonDefaults.shapesFor(size),
                        colors = colors,
                        onClick = { showQueueBottomSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = stringResource(R.string.common_queue),
                                modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                            )

                            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))

                            Text(
                                text = stringResource(R.string.common_queue),
                                style = ButtonDefaults.textStyleFor(size)
                            )
                        }
                    }
                }
            }
        }

        if(showSleepTimerBottomSheet) MediaPlayerSleepTimerBottomSheet {
            showSleepTimerBottomSheet = false
        }

        if(showSpeedBottomSheet) MediaPlayerSpeedBottomSheet {
            showSpeedBottomSheet = false
        }

        if(showQueueBottomSheet) MediaPlayerQueueBottomSheet(
            onOpenEpisode = { origin, id ->
                dismissGracefully {
                    onOpenEpisode(origin, id)
                }
            }
        ) {
            showQueueBottomSheet = false
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Image(
    mediaPlayerViewModel: MediaPlayerViewModel,
    compactLayout: Boolean
) {
    val size = when(compactLayout) {
        true -> 96.dp
        false -> 256.dp
    }

    AnimatedContent(mediaPlayerViewModel.isLoading) { isLoading ->
        when(isLoading) {
            true -> {
                Box(
                    Modifier.size(size),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator(
                        Modifier.size(size / 2)
                    )
                }
            }

            false -> {
                AnimatedContent(mediaPlayerViewModel.mediaMetadata) { mediaMetadata ->
                    ShimmerAsyncImage(
                        modifier = Modifier
                            .size(size)
                            .clip(RoundedCornerShape(16.dp)),

                        model = mediaMetadata?.artworkUri,
                        contentDescription = null,

                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}