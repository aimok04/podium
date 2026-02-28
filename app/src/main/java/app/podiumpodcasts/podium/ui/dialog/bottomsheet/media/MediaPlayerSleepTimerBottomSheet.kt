package app.podiumpodcasts.podium.ui.dialog.bottomsheet.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.MediaPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPlayerSleepTimerBottomSheet(
    onDismiss: () -> Unit
) {
    val vm = viewModel<MediaPlayerViewModel>()

    var isSleepTimerEnabled by remember { mutableStateOf(false) }
    var sliderSleepTimerState by remember { mutableStateOf<Float?>(null) }

    val sleepTimerState = vm.getSleepTimerState()

    LaunchedEffect(sleepTimerState.value) {
        isSleepTimerEnabled = sleepTimerState.value != null
        sliderSleepTimerState = sleepTimerState.value?.toFloat()
    }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(isSleepTimerEnabled) { isEnabled ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = stringResource(R.string.common_sleep_timer),
                        tint = when(isEnabled) {
                            true -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = when(isEnabled) {
                            true -> stringResource(
                                R.string.template_min,
                                (sliderSleepTimerState ?: 5).toInt()
                            )

                            else -> stringResource(R.string.common_sleep_timer_off)
                        },
                        color = when(isEnabled) {
                            true -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = Typography.headlineMediumEmphasized
                    )
                }
            }

            Slider(
                value = sliderSleepTimerState ?: 0f,
                valueRange = 1f..90f,
                steps = 17,
                onValueChange = {
                    sliderSleepTimerState = when(it) {
                        1f -> null
                        else -> it
                    }

                    isSleepTimerEnabled = sliderSleepTimerState != null
                },
                onValueChangeFinished = {
                    vm.setSleepTimer(
                        sliderSleepTimerState?.let { value ->
                            System.currentTimeMillis() + (value.toInt() * 60L * 1000L)
                        }
                    )
                }
            )
        }
    }
}